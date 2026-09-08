package com.xai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.xai.api.batch.Batch;
import com.xai.api.batch.BatchAddRequest;
import com.xai.api.batch.BatchListResponse;
import com.xai.api.batch.BatchMetadata;
import com.xai.api.batch.BatchMetadataListResponse;
import com.xai.api.batch.BatchRequest;
import com.xai.api.batch.BatchResult;
import com.xai.api.batch.BatchResultsResponse;
import com.xai.api.batch.BatchState;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * REST client for {@code /v1/batches} plus optional per-instance watch/poll.
 * <p>
 * {@link #submit(Object)} creates a one-item batch and returns its id.
 * {@link #submit(String, Object)} appends to an existing batch. If a
 * {@link BatchListener} is set, submit starts a ticker; the ticker stops when
 * no jobs remain.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
public class XaiBatchClient extends XaiAbstractClient {

  private static final Logger LOG = Logger.getLogger(XaiBatchClient.class.getName());
  private static final Duration DEFAULT_POLL = Duration.ofSeconds(5);
  private static final Duration MIN_POLL = Duration.ofSeconds(1);
  private static final int PAGE_SIZE = 100;
  private static final DateTimeFormatter SUBMIT_TIME
    = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

  private final Clock clock;
  private final ConcurrentHashMap.KeySetView<String, Boolean> watches
    = ConcurrentHashMap.newKeySet();
  private final AtomicBoolean inFlight = new AtomicBoolean();
  private final AtomicBoolean closed = new AtomicBoolean();
  private final Object tickerLock = new Object();

  private volatile Duration pollInterval;
  private volatile BatchListener listener;
  private ScheduledExecutorService executor;
  private ScheduledFuture<?> future;

  public XaiBatchClient() {
    this(XaiClientConfig.readConfig());
  }

  public XaiBatchClient(XaiClientConfig config) {
    this(config, DEFAULT_POLL, Clock.systemDefaultZone());
  }

  XaiBatchClient(XaiClientConfig config, Duration pollInterval, Clock clock) {
    super("", config);
    this.clock = Objects.requireNonNull(clock, "clock");
    setPollInterval(pollInterval);
  }

  /**
   * New batch with exactly one item. Name is {@code "{Type} submitted at h:mm a"}.
   *
   * @param request a {@link BatchRequest} payload type
   * @return server {@code batch_id}
   */
  public String submit(Object request) {
    BatchRequest payload = toBatchRequest(request);
    Batch created = create(submitName(request, clock));
    if (created == null || created.getBatchId() == null || created.getBatchId().isBlank()) {
      throw new ApiHttpException("create batch returned no batch_id");
    }
    addOne(created.getBatchId(), payload);
    track(created.getBatchId());
    return created.getBatchId();
  }

  /**
   * Append one item to an existing batch.
   *
   * @param batchId existing batch
   * @param request a {@link BatchRequest} payload type
   * @return {@code batchId}
   */
  public String submit(String batchId, Object request) {
    requireBatchId(batchId);
    addOne(batchId, toBatchRequest(request));
    track(batchId);
    return batchId;
  }

  public Batch create(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name");
    }
    Batch body = new Batch();
    body.setName(name);
    HttpRequest http = doPostJson("/batches", body);
    return sendRequest(http, Batch.class);
  }

  public Batch get(String batchId) {
    requireBatchId(batchId);
    HttpRequest http = doGet("/batches/" + batchId);
    return sendRequest(http, Batch.class);
  }

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

  public Batch cancel(String batchId) {
    requireBatchId(batchId);
    // colon RPC; empty JSON object is a valid POST body
    HttpRequest http = doPostJson("/batches/" + batchId + ":cancel", Collections.emptyMap());
    return sendRequest(http, Batch.class);
  }

  public void setListener(BatchListener listener) {
    this.listener = listener;
  }

  public Duration getPollInterval() {
    return pollInterval;
  }

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

  @Override
  public void close() throws Exception {
    closed.set(true);
    watches.clear();
    synchronized (tickerLock) {
      stopTickerLocked();
    }
    super.close();
  }

  static BatchRequest toBatchRequest(Object request) {
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
      // same REST key as generate
      wrapped.setVideoGeneration(request);
    } else {
      throw new IllegalArgumentException("Unsupported batch payload: " + request.getClass().getName());
    }
    return wrapped;
  }

  static String submitName(Object request, Clock clock) {
    String when = LocalTime.now(clock).format(SUBMIT_TIME);
    return request.getClass().getSimpleName() + " submitted at " + when;
  }

  static boolean isComplete(Batch batch) {
    if (batch == null || batch.getState() == null) {
      return false;
    }
    BatchState state = batch.getState();
    return state.getNumRequests() > 0 && state.getNumPending() == 0;
  }

  static String pageQuery(Integer limit, String paginationToken) {
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

  private void addOne(String batchId, BatchRequest payload) {
    BatchAddRequest item = new BatchAddRequest();
    item.setBatchRequestId(UUID.randomUUID().toString());
    item.setBatchRequest(payload);
    List<BatchAddRequest> items = new ArrayList<>();
    items.add(item);
    add(batchId, items);
  }

  private void track(String batchId) {
    if (listener == null) {
      return;
    }
    watches.add(batchId);
    startTicker();
  }

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
      LOG.log(Level.INFO, "POLL ok '{'batchId={0}, pending={1}, time={2} ms'}'",
        new Object[]{batchId, batch.getState() == null ? null : batch.getState().getNumPending(), time});
      notifyProgress(batch);
      if (!isComplete(batch)) {
        return;
      }
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

  private static boolean hasNextPage(String token) {
    return token != null && !token.isBlank();
  }

  private static void requireBatchId(String batchId) {
    if (batchId == null || batchId.isBlank()) {
      throw new IllegalArgumentException("batchId");
    }
  }

}
