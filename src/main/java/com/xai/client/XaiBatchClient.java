package com.xai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.xai.api.batch.*;
import com.xai.api.images.EditImageRequest;
import com.xai.api.images.GenerateImageRequest;
import com.xai.api.responses.ModelRequest;
import com.xai.api.video.EditVideoRequest;
import com.xai.api.video.GenerateVideoRequest;
import com.xai.client.exception.ApiHttpException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST client for {@code /v1/batches} plus optional per-instance watch/poll.
 * <p>
 * Client for the XAI Batch API supporting asynchronous submission and
 * processing of model, image, and video requests.
 * <p>
 * This client provides both direct batch management operations and optional
 * background polling with a {@link BatchListener} for progress and completion
 * notifications. All public methods are safe for concurrent use. The internal
 * polling mechanism is designed to be resource-efficient, starting a scheduler
 * only when watches are active and stopping it when no batches remain.
 * <p>
 * Batch lifecycle: create a batch, add one or more requests, then poll or
 * listen for completion. Results are delivered as a {@link JsonNode} to allow
 * flexible downstream processing without forcing a specific model.
 * <p>
 * Thread safety is achieved through atomic flags, a concurrent key set for
 * watches, and explicit synchronization only around executor lifecycle changes.
 * Polling operations are serialized via an in-flight guard to prevent overlap.
 *
 * @author XAI
 * @since 1.0
 */
public class XaiBatchClient extends XaiAbstractClient {

  // Developer note: Logger is package-private static to allow subclass or test
  // visibility if needed while keeping implementation details encapsulated.
  private static final Logger LOG = Logger.getLogger(XaiBatchClient.class.getName());

  // Developer note: DEFAULT_POLL and MIN_POLL are chosen to balance API load
  // against responsiveness. MIN_POLL of 1s prevents accidental denial-of-service
  // from overly aggressive user configuration.
  private static final Duration DEFAULT_POLL = Duration.ofSeconds(5);
  private static final Duration MIN_POLL = Duration.ofSeconds(1);

  // Developer note: PAGE_SIZE is fixed at 100 to match typical API pagination
  // limits and reduce the number of round-trips during list operations.
  private static final int PAGE_SIZE = 100;

  // Developer note: SUBMIT_TIME uses a human-readable format for auto-generated
  // batch names. Clock.systemUTC() ensures consistent naming regardless of
  // local timezone.
  private static final DateTimeFormatter SUBMIT_TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

  /**
   * Thread-safe set of batch IDs currently under active polling.
   * <p>
   * Implemented via {@link ConcurrentHashMap#newKeySet()} to support concurrent
   * add/remove without external locking during normal operation. The set is
   * cleared on {@link #close()}.
   */
  private final ConcurrentHashMap.KeySetView<String, Boolean> watches = ConcurrentHashMap.newKeySet();

  /**
   * Atomic flag used to serialize polling cycles.
   * <p>
   * Prevents overlapping executions of {@link #tick()} when the scheduled
   * interval is shorter than actual poll latency.
   */
  private final AtomicBoolean inFlight = new AtomicBoolean();

  /**
   * Atomic flag indicating the client has been closed.
   * <p>
   * Once set, new tracking requests are rejected and the scheduler is stopped.
   */
  private final AtomicBoolean closed = new AtomicBoolean();

  /**
   * Lock object used exclusively for executor and future lifecycle operations.
   * <p>
   * Minimizes contention by protecting only the narrow window of scheduler
   * creation, restart, and shutdown.
   */
  private final Object tickerLock = new Object();

  /**
   * Current polling interval. Volatile to allow safe publication across
   * threads.
   */
  private volatile Duration pollInterval;

  /**
   * User-supplied listener for batch events. Volatile to allow safe
   * publication.
   * <p>
   * Snapshotted before each notification to avoid races with
   * {@link #setListener(BatchListener)}.
   */
  private volatile BatchListener listener;

  /**
   * Single-threaded scheduled executor responsible for periodic polling.
   * <p>
   * Lazily created and daemon=false to ensure pending work completes during
   * graceful shutdown. Null when no watches are active.
   */
  private ScheduledExecutorService executor;

  /**
   * Handle to the currently scheduled polling task.
   * <p>
   * Null when the scheduler is stopped or has no active watches.
   */
  private ScheduledFuture<?> future;

  /**
   * Creates a new client using the default configuration read from the
   * environment.
   * <p>
   * Equivalent to {@code new XaiBatchClient(XaiClientConfig.readConfig())}.
   */
  public XaiBatchClient() {
    this(XaiClientConfig.readConfig());
  }

  /**
   * Creates a new client with the supplied configuration and the default poll
   * interval of 5 seconds.
   *
   * @param config client configuration containing API key and base URL
   */
  public XaiBatchClient(XaiClientConfig config) {
    this(config, DEFAULT_POLL);
  }

  /**
   * Package-private constructor used for testing.
   * <p>
   * Allows injection of a custom poll interval while still performing the
   * required super-class initialization.
   *
   * @param config       client configuration
   * @param pollInterval initial polling interval (must be &gt;= 1s)
   */
  XaiBatchClient(XaiClientConfig config, Duration pollInterval) {
    super("", config);
    setPollInterval(pollInterval);
  }

  /**
   * Submits a single request by creating a new batch and adding the request to
   * it.
   * <p>
   * The batch is automatically tracked if a listener has been registered. The
   * generated batch name includes the request type and submission time.
   *
   * @param request the request object (ModelRequest, GenerateImageRequest,
   *                EditImageRequest, GenerateVideoRequest, or EditVideoRequest)
   * @return the newly created batch ID
   * @throws IllegalArgumentException if the request type is unsupported or null
   * @throws ApiHttpException         if the server rejects the create or add
   *                                  operation
   */
  public String submit(Object request) {
    BatchRequest payload = toBatchRequest(request);
    String batchName = generateBatchName(request);
    Batch created = create(batchName);
    if (created == null || created.getBatchId() == null || created.getBatchId().isBlank()) {
      throw new ApiHttpException("create batch returned no batch_id");
    }
    addOne(created.getBatchId(), payload);
    track(created.getBatchId());
    return created.getBatchId();
  }

  /**
   * Adds a single request to an existing batch and begins tracking it.
   * <p>
   * Use this overload when the caller has already created a batch via
   * {@link #create(String)} or wishes to add to a previously submitted batch.
   *
   * @param batchId the target batch identifier
   * @param request the request payload
   * @return the batchId (for fluent usage)
   * @throws IllegalArgumentException if batchId is blank or request type is
   *                                  unsupported
   * @throws ApiHttpException         if the add operation fails
   */
  public String submit(String batchId, Object request) {
    requireBatchId(batchId);
    addOne(batchId, toBatchRequest(request));
    track(batchId);
    return batchId;
  }

  /**
   * Creates a new empty batch with the given name.
   * <p>
   * The name is used for human identification in the XAI dashboard and logs.
   * After creation, use {@link #add(String, List)} or
   * {@link #submit(String, Object)} to populate the batch.
   *
   * @param name human-readable batch name (must be non-blank)
   * @return the created batch metadata including the generated batchId
   * @throws IllegalArgumentException if name is null or blank
   * @throws ApiHttpException         on transport or server error
   */
  public Batch create(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name");
    }
    Batch body = new Batch();
    body.setName(name);
    HttpRequest http = doPostJson("/batches", body);
    return sendRequest(http, Batch.class);
  }

  /**
   * Retrieves the current state of a single batch.
   *
   * @param batchId the batch identifier
   * @return the batch including its current {@link BatchState}
   * @throws IllegalArgumentException if batchId is blank
   * @throws ApiHttpException         on transport or server error
   */
  public Batch get(String batchId) {
    requireBatchId(batchId);
    HttpRequest http = doGet("/batches/" + batchId);
    return sendRequest(http, Batch.class);
  }

  /**
   * Lists all batches visible to the current credentials.
   * <p>
   * Performs automatic pagination using the server's pagination token until all
   * pages have been retrieved.
   *
   * @return list of all batches (may be empty)
   */
  public List<Batch> list() {
    List<Batch> all = new ArrayList<>();
    String token = null;
    do {
      HttpRequest http = doGet("/batches" + pageQuery(PAGE_SIZE, token));
      BatchListResponse page = sendRequest(http, BatchListResponse.class);
      if (page == null) {
        break;
      }
      if (page.getBatches() != null) {
        all.addAll(page.getBatches());
      }
      token = page.getPaginationToken();
    } while (hasNextPage(token));
    return all;
  }

  /**
   * Adds multiple pre-constructed batch request items to an existing batch.
   * <p>
   * Each item in the list must contain a unique batchRequestId and the actual
   * payload wrapped in a {@link BatchRequest}.
   *
   * @param batchId  target batch identifier
   * @param requests list of batch add requests (non-empty)
   * @throws IllegalArgumentException if batchId is blank or requests is
   *                                  null/empty
   * @throws ApiHttpException         on transport or server error
   */
  public void add(String batchId, List<BatchAddRequest> requests) {
    requireBatchId(batchId);
    if (requests == null || requests.isEmpty()) {
      throw new IllegalArgumentException("requests");
    }
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("batch_requests", requests);
    HttpRequest http = doPostJson("/batches/" + batchId + "/requests", body);
    sendRequest(http, Void.class);
  }

  /**
   * Lists all request metadata for a given batch with automatic pagination.
   *
   * @param batchId the batch identifier
   * @return list of {@link BatchMetadata} entries
   * @throws IllegalArgumentException if batchId is blank
   */
  public List<BatchMetadata> listRequests(String batchId) {
    requireBatchId(batchId);
    List<BatchMetadata> all = new ArrayList<>();
    String token = null;
    do {
      HttpRequest http = doGet("/batches/" + batchId + "/requests" + pageQuery(PAGE_SIZE, token));
      BatchMetadataListResponse page = sendRequest(http, BatchMetadataListResponse.class);
      if (page == null) {
        break;
      }
      if (page.getBatchRequestMetadata() != null) {
        all.addAll(page.getBatchRequestMetadata());
      }
      token = page.getPaginationToken();
    } while (hasNextPage(token));
    return all;
  }

  /**
   * Lists all completed results for a given batch with automatic pagination.
   * <p>
   * Results are only available after the batch reaches a terminal state. The
   * returned objects contain the original requestId and the response payload.
   *
   * @param batchId the batch identifier
   * @return list of {@link BatchResult} entries
   * @throws IllegalArgumentException if batchId is blank
   */
  public List<BatchResult> listResults(String batchId) {
    requireBatchId(batchId);
    List<BatchResult> all = new ArrayList<>();
    String token = null;
    do {
      HttpRequest http = doGet("/batches/" + batchId + "/results" + pageQuery(PAGE_SIZE, token));
      BatchResultsResponse page = sendRequest(http, BatchResultsResponse.class);
      if (page == null) {
        break;
      }
      if (page.getResults() != null) {
        all.addAll(page.getResults());
      }
      token = page.getPaginationToken();
    } while (hasNextPage(token));
    return all;
  }

  /**
   * Requests cancellation of a batch.
   * <p>
   * Cancellation is best-effort. Already running requests may still complete.
   * The returned Batch reflects the state immediately after the cancel request.
   *
   * @param batchId the batch to cancel
   * @return updated batch state
   * @throws IllegalArgumentException if batchId is blank
   * @throws ApiHttpException         on transport or server error
   */
  public Batch cancel(String batchId) {
    requireBatchId(batchId);
    // Developer note: The ":cancel" suffix follows the XAI Batch API convention
    // for action endpoints as documented in the batch API reference.
    HttpRequest http = doPostJson("/batches/" + batchId + ":cancel", Collections.emptyMap());
    return sendRequest(http, Batch.class);
  }

  /**
   * Registers a listener that will be notified of batch progress, completion,
   * and errors for any batch submitted through this client.
   * <p>
   * Setting a listener to null disables notifications. The listener reference
   * is snapshotted on each event to avoid races during listener replacement.
   *
   * @param listener the listener to receive callbacks, or null to disable
   */
  public void setListener(BatchListener listener) {
    this.listener = listener;
  }

  /**
   * Returns the current polling interval used for background batch tracking.
   *
   * @return the active poll interval (never null)
   */
  public Duration getPollInterval() {
    return pollInterval;
  }

  /**
   * Updates the polling interval used for background tracking.
   * <p>
   * If background polling is currently active, the scheduler is restarted with
   * the new interval. The change takes effect on the next scheduled tick.
   *
   * @param interval new polling interval (must be at least 1 second)
   * @throws IllegalArgumentException if interval is null or less than 1s
   */
  public void setPollInterval(Duration interval) {
    if (interval == null || interval.compareTo(MIN_POLL) < 0) {
      throw new IllegalArgumentException("pollInterval must be at least 1s");
    }
    this.pollInterval = interval;
    synchronized (tickerLock) {
      if (executor != null && !watches.isEmpty()) {
        restartTickerLocked();
      }
    }
  }

  /**
   * Closes the client, stops all background polling, and clears watched
   * batches.
   * <p>
   * After close, further tracking requests will throw
   * {@link IllegalStateException}. The underlying HTTP client is also closed.
   *
   * @throws Exception if the underlying client fails to close
   */
  @Override
  public void close() throws Exception {
    closed.set(true);
    watches.clear();
    synchronized (tickerLock) {
      stopTickerLocked();
    }
    super.close();
  }

  /**
   * Converts a user-facing request object into the internal
   * {@link BatchRequest} wrapper.
   * <p>
   * Supports the five known request types. Video edit requests are
   * intentionally routed to the videoGeneration slot to match current server
   * expectations.
   *
   * @param request the user request
   * @return wrapped batch request
   * @throws IllegalArgumentException for null or unsupported request types
   */
  private BatchRequest toBatchRequest(Object request) {
    if (request == null) {
      throw new IllegalArgumentException("request");
    }
    BatchRequest wrapped = new BatchRequest();
    if (request instanceof ModelRequest) {
      wrapped.setResponses((ModelRequest) request);
    } else if (request instanceof GenerateImageRequest) {
      wrapped.setImageGeneration((GenerateImageRequest) request);
    } else if (request instanceof EditImageRequest) {
      wrapped.setImageEdit((EditImageRequest) request);
    } else if (request instanceof GenerateVideoRequest) {
      wrapped.setVideoGeneration(request);
    } else if (request instanceof EditVideoRequest) {
      // Developer note: EditVideoRequest is mapped to the same videoGeneration
      // field as GenerateVideoRequest per current API contract. This may change
      // in future versions.
      wrapped.setVideoGeneration(request);
    } else {
      throw new IllegalArgumentException("Unsupported batch payload: " + request.getClass().getName());
    }
    return wrapped;
  }

  /**
   * Generates a descriptive batch name containing the request class and UTC
   * time.
   * <p>
   * Used only for the single-request submit convenience method.
   */
  private String generateBatchName(Object request) {
    String when = LocalTime.now(Clock.systemUTC()).format(SUBMIT_TIME);
    return request.getClass().getSimpleName() + " submitted at " + when;
  }

  /**
   * Determines whether a batch has finished processing all of its requests.
   * <p>
   * A batch is considered complete only when it has at least one request and
   * zero pending requests. Null or incomplete state objects return false.
   *
   * @param batch the batch to inspect
   * @return true if all requests have reached a terminal state
   */
  private boolean isComplete(Batch batch) {
    if (batch == null || batch.getState() == null) {
      return false;
    }
    BatchState state = batch.getState();
    return state.getNumRequests() > 0 && state.getNumPending() == 0;
  }

  /**
   * Builds a query string for paginated list operations.
   * <p>
   * Properly URL-encodes the pagination token when present.
   */
  private String pageQuery(Integer limit, String paginationToken) {
    StringBuilder sb = new StringBuilder();
    if (limit != null) {
      sb.append("limit=").append(limit.intValue());
    }
    if (paginationToken != null && !paginationToken.isBlank()) {
      if (sb.length() > 0) {
        sb.append("&");
      }
      sb.append("pagination_token=");
      sb.append(URLEncoder.encode(paginationToken, StandardCharsets.UTF_8));
    }
    return sb.length() == 0 ? "" : "?" + sb;
  }

  /**
   * Convenience wrapper that adds exactly one request to a batch.
   * <p>
   * Generates a random batchRequestId for the item.
   */
  private void addOne(String batchId, BatchRequest payload) {
    BatchAddRequest item = new BatchAddRequest();
    item.setBatchRequestId(UUID.randomUUID().toString());
    item.setBatchRequest(payload);
    List<BatchAddRequest> items = new ArrayList<>();
    items.add(item);
    add(batchId, items);
  }

  /**
   * Begins tracking the given batch for background polling.
   * <p>
   * No-op if no listener is currently registered.
   */
  private void track(String batchId) {
    if (listener == null) {
      return;
    }
    watches.add(batchId);
    startTicker();
  }

  /**
   * Ensures a background polling thread exists and is scheduled.
   * <p>
   * Creates a non-daemon single-threaded executor if necessary. The thread is
   * named "xai-batch-poller" for easier diagnostics in thread dumps.
   */
  private void startTicker() {
    if (closed.get()) {
      throw new IllegalStateException("client is closed");
    }
    synchronized (tickerLock) {
      if (executor == null || executor.isShutdown()) {
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
          Thread thread = new Thread(r, "xai-batch-poller");
          thread.setDaemon(false);
          return thread;
        });
      }
      if (future == null || future.isCancelled()) {
        long ms = pollInterval.toMillis();
        future = executor.scheduleAtFixedRate(this::tick, ms, ms, TimeUnit.MILLISECONDS);
      }
    }
  }

  /**
   * Restarts the scheduled polling task with the current poll interval.
   * <p>
   * Must be called while holding {@code tickerLock}. Only restarts if watches
   * are still present.
   */
  private void restartTickerLocked() {
    if (future != null) {
      future.cancel(false);
      future = null;
    }
    if (executor != null && !executor.isShutdown() && !watches.isEmpty()) {
      long ms = pollInterval.toMillis();
      future = executor.scheduleAtFixedRate(this::tick, ms, ms, TimeUnit.MILLISECONDS);
    }
  }

  /**
   * Stops the scheduler and releases the executor.
   * <p>
   * Must be called while holding {@code tickerLock}. Does not wait for
   * termination; shutdown is best-effort.
   */
  private void stopTickerLocked() {
    if (future != null) {
      future.cancel(false);
      future = null;
    }
    if (executor != null) {
      executor.shutdown();
      executor = null;
    }
  }

  /**
   * Periodic polling task executed by the scheduled executor.
   * <p>
   * Uses an atomic in-flight guard to drop overlapping executions. After
   * processing, stops the scheduler if no watches remain.
   */
  private void tick() {
    if (!inFlight.compareAndSet(false, true)) {
      return;
    }
    try {
      for (String batchId : new ArrayList<>(watches)) {
        pollOne(batchId);
      }
    } finally {
      inFlight.set(false);
      if (watches.isEmpty()) {
        synchronized (tickerLock) {
          if (watches.isEmpty()) {
            stopTickerLocked();
          }
        }
      }
    }
  }

  /**
   * Performs a single poll for the given batch and notifies the listener.
   * <p>
   * On completion, the batchId is removed from the watch set and results are
   * fetched and delivered as a JsonNode. Errors are logged and delivered via
   * onError.
   */
  private void pollOne(String batchId) {
    long start = System.currentTimeMillis();
    try {
      Batch batch = get(batchId);
      long time = System.currentTimeMillis() - start;
      if (batch == null) {
        watches.remove(batchId);
        LOG.log(Level.INFO, "POLL failed '{'batchId={0}, error=not found, time={1} ms'}'",
                new Object[]{batchId, time});
        notifyError(batchId, new ApiHttpException("batch not found: " + batchId));
        return;
      }
      LOG.log(Level.INFO,
              "POLL ok '{'batchId={0}, pending={1}, time={2} ms'}'",
              new Object[]{batchId, batch.getState() == null ? null : batch.getState().getNumPending(), time});
      notifyProgress(batch);
      if (!isComplete(batch)) {
        return;
      }
      // Developer note: Results are converted to JsonNode rather than a typed
      // list so that the listener can handle heterogeneous response shapes
      // without requiring knowledge of every possible result type.
      JsonNode results = mapper.valueToTree(listResults(batchId));
      watches.remove(batchId);
      notifyComplete(batchId, results);
    } catch (RuntimeException ex) {
      long time = System.currentTimeMillis() - start;
      LOG.log(Level.INFO, "POLL failed '{'batchId={0}, error={1}, time={2} ms'}'",
              new Object[]{batchId, ex.getMessage(), time});
      notifyError(batchId, ex);
    }
  }

  /**
   * Delivers a progress notification to the listener.
   * <p>
   * Listener exceptions are caught and logged at WARNING level so that a
   * misbehaving listener cannot break the polling loop.
   */
  private void notifyProgress(Batch batch) {
    BatchListener snap = listener;
    if (snap == null) {
      return;
    }
    try {
      snap.onProgress(batch);
    } catch (RuntimeException ex) {
      LOG.log(Level.WARNING, "LISTEN failed '{'batchId={0}, error={1}'}'",
              new Object[]{batch.getBatchId(), ex.getMessage()});
    }
  }

  /**
   * Delivers a completion notification containing the batch results.
   * <p>
   * Listener exceptions are swallowed to protect the polling thread.
   */
  private void notifyComplete(String batchId, JsonNode results) {
    BatchListener snap = listener;
    if (snap == null) {
      return;
    }
    try {
      snap.onComplete(batchId, results);
    } catch (RuntimeException ex) {
      LOG.log(Level.WARNING, "LISTEN failed '{'batchId={0}, error={1}'}'",
              new Object[]{batchId, ex.getMessage()});
    }
  }

  /**
   * Delivers an error notification for a batch.
   * <p>
   * Listener exceptions are swallowed to protect the polling thread.
   */
  private void notifyError(String batchId, Throwable error) {
    BatchListener snap = listener;
    if (snap == null) {
      return;
    }
    try {
      snap.onError(batchId, error);
    } catch (RuntimeException ex) {
      LOG.log(Level.WARNING, "LISTEN failed '{'batchId={0}, error={1}'}'",
              new Object[]{batchId, ex.getMessage()});
    }
  }

  /**
   * Returns true if the pagination token indicates another page exists.
   */
  private boolean hasNextPage(String token) {
    return token != null && !token.isBlank();
  }

  /**
   * Validates that a batch identifier is present and non-blank.
   *
   * @throws IllegalArgumentException if batchId is null or blank
   */
  private void requireBatchId(String batchId) {
    if (batchId == null || batchId.isBlank()) {
      throw new IllegalArgumentException("batchId");
    }
  }

}
