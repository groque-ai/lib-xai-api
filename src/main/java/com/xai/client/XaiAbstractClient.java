package com.xai.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.xai.client.exception.ApiHttpException;
import com.xai.client.exception.ApiParseException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Abstract base implementation for XAI API clients providing common HTTP, JSON,
 * retry, and streaming infrastructure.
 * <p>
 * This class manages an {@link HttpClient}, {@link ObjectMapper}, and
 * configuration for all concrete XAI clients. It is intended to be subclassed
 * by service-specific clients that use the protected helper methods.
 * <p>
 * Thread-safety: The class is designed for concurrent use. Static sinks are
 * volatile and intended only for debugging/testing. Instance state is
 * effectively immutable after construction except for the baseUrl field which
 * is not mutated after initialization in normal usage.
 * <p>
 * Resource management: The underlying {@link HttpClient} is not closed by this
 * implementation (as recommended by the JDK for shared clients). Subclasses
 * should override {@link #close()} if they hold additional resources.
 */
public abstract class XaiAbstractClient implements AutoCloseable {

  // DEVELOPER NOTE: Hard-coded default base URI used when no custom base URL is provided via config.
  // This allows easy override for testing or alternative environments while keeping a safe production default.
  private static final String BASE_URI = "https://api.x.ai";

  // DEVELOPER NOTE: Logger is package-private visibility via static for use across the hierarchy and for test injection.
  // Using java.util.logging to avoid external logging framework dependencies.
  private static final Logger LOG = Logger.getLogger(XaiAbstractClient.class.getName());

  /**
   * Client configuration containing API key, timeouts, and base URL overrides.
   * <p>
   * Final and non-null after construction. Used for all request building and
   * timeout configuration.
   */
  protected final XaiClientConfig config;

  /**
   * Shared HTTP/2 client instance configured with version, connect timeout, and
   * redirect policy.
   * <p>
   * Created once per client instance. Not closed by {@link #close()} because
   * the JDK recommends sharing HttpClient instances across an application.
   */
  protected final HttpClient httpClient;

  /**
   * Resolved base URL for this client instance (root + path segment).
   * <p>
   * Protected to allow subclasses to inspect or (in rare cases) adjust path
   * resolution.
   */
  protected String baseUrl;

  /**
   * Pre-configured Jackson ObjectMapper used for all serialization and
   * deserialization.
   * <p>
   * Configuration is intentionally strict about nulls and unknown properties
   * while being lenient with unknown enum values. ORDER_MAP_ENTRIES_BY_KEYS is
   * enabled for deterministic output.
   */
  protected final ObjectMapper mapper;

  /**
   * Constructs a new client using the default configuration read from the
   * environment or classpath.
   * <p>
   * Delegates to the two-argument constructor after loading configuration.
   *
   * @param path the API path segment to append to the base URL (e.g.
   *             "/v1/chat")
   */
  protected XaiAbstractClient(String path) {
    this(path, XaiClientConfig.readConfig());
  }

  /**
   * Constructs a new client with explicit configuration.
   * <p>
   * Performs base URL normalization, creates the HttpClient, and initializes
   * the ObjectMapper.
   *
   * @param path   the API path segment to append to the base URL
   * @param config the client configuration; must not be null
   * @throws NullPointerException if config is null
   */
  protected XaiAbstractClient(String path, XaiClientConfig config) {
    this.config = Objects.requireNonNull(config, "config");
    String root = config.getBaseUrl();
    if (root == null || root.isBlank()) {
      root = BASE_URI;
    }
    if (root.endsWith("/")) {
      root = root.substring(0, root.length() - 1);
    }
    this.baseUrl = root + path;

    // DEVELOPER NOTE: HTTP/2 is explicitly requested for modern performance characteristics.
    // Redirect policy is derived directly from config rather than hard-coded.
    this.httpClient = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .connectTimeout(config.getConnectTimeout())
      .followRedirects(config.isFollowRedirects()
                       ? HttpClient.Redirect.NORMAL
                       : HttpClient.Redirect.NEVER)
      .build();

    // DEVELOPER NOTE: SerializationInclusion.NON_NULL avoids sending null fields.
    // FAIL_ON_UNKNOWN_PROPERTIES is disabled to be resilient to API evolution.
    // PROPAGATE_TRANSIENT_MARKER is enabled so that @JsonIgnore on transient fields is respected.
    this.mapper = new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
      .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
      .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER);

  }

  /**
   * Closes this client.
   * <p>
   * Current implementation is a no-op because the shared {@link HttpClient}
   * should not be closed by individual clients. Subclasses may override to
   * release additional resources.
   *
   * @throws Exception if an error occurs during close (never thrown by base
   *                   implementation)
   */
  @Override
  public void close() throws Exception {
    // DEVELOPER NOTE: Intentionally left empty. HttpClient instances are meant to be long-lived
    // and shared. Closing them can cause issues for other clients using the same underlying resources.
  }

  /**
   * Sends a synchronous HTTP request and deserializes the response to the given
   * class.
   *
   * @param request      the prepared HttpRequest
   * @param responseType the target class for deserialization (use Void.class
   *                     for no body)
   * @param <T>          the response type
   * @return the deserialized response or null for 404 or Void.class
   * @throws ApiHttpException  if the request fails or returns a non-success
   *                           status
   * @throws ApiParseException if JSON deserialization fails
   */
  protected <T> T sendRequest(HttpRequest request, Class<T> responseType) {
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return handleResponse(response, responseType);
    } catch (IOException | InterruptedException ex) {
      String clazz = (responseType == null) ? "null" : responseType.getSimpleName();
      throw new ApiHttpException("API request error: " + request.uri() + " " + clazz, ex);
    }
  }

  /**
   * Sends a synchronous HTTP request and deserializes the response using a
   * TypeReference.
   * <p>
   * Use this overload for generic types such as List or Map.
   *
   * @param request      the prepared HttpRequest
   * @param responseType the TypeReference for deserialization
   * @param <T>          the response type
   * @return the deserialized response
   * @throws ApiHttpException  if the request fails or returns a non-success
   *                           status
   * @throws ApiParseException if JSON deserialization fails
   */
  protected <T> T sendRequest(HttpRequest request, TypeReference<T> responseType) {
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return handleResponse(response, responseType);
    } catch (IOException | InterruptedException ex) {
      String type = (responseType == null) ? "null" : responseType.getType().getTypeName();
      throw new ApiHttpException("API request error: " + request.uri() + " " + type, ex);
    }
  }

  /**
   * Handles a successful or error response for class-based deserialization.
   * <p>
   * Special handling for 404 returns null instead of throwing (legacy
   * behavior).
   *
   * @param response the HTTP response
   * @param type     the target class
   * @param <T>      response type
   * @return deserialized object or null
   * @throws ApiParseException if deserialization fails
   */
  private <T> T handleResponse(HttpResponse<String> response, Class<T> type) {
    try {
      int status = response.statusCode();
      String body = response.body();
      if (status >= 200 && status < 300) {
        if (type == Void.class) {
          return null;
        }
        return mapper.readValue(body, type);
      } else if (status == 404) {
        // DEVELOPER NOTE: 404 is deliberately treated as a non-error returning null.
        // This matches historical API client behavior for optional resource lookups.
        LOG.info(this.getClass().getSimpleName() + " error (not found) {status=" + status + ", body=" + body + "}");
        return null;
      } else {
        throw new ApiHttpException("API error {status=" + status + ", body=" + body + "}");
      }
    } catch (JsonProcessingException ex) {
      String clazz = (type == null) ? "null" : type.getSimpleName();
      throw new ApiParseException("Response serialization error for " + clazz, ex);
    }
  }

  /**
   * Handles a response using a TypeReference.
   * <p>
   * Note: This private overload does not perform special 404 handling.
   *
   * @param response the HTTP response
   * @param type     the TypeReference
   * @param <T>      response type
   * @return deserialized object
   * @throws IOException if deserialization or API error occurs
   */
  private <T> T handleResponse(HttpResponse<String> response, TypeReference<T> type) throws IOException {
    int status = response.statusCode();
    String body = response.body();
    if (status >= 200 && status < 300) {
      return mapper.readValue(body, type);
    } else {
      throw new IOException("API error: " + status + " " + body);
    }
  }

  /**
   * Creates a pre-populated {@link HttpRequest.Builder} for the given relative
   * path.
   * <p>
   * Automatically adds Authorization, Content-Type, and Accept headers.
   *
   * @param path the relative path (should start with /)
   * @return a builder ready for method and body configuration
   */
  protected HttpRequest.Builder buildRequest(String path) {
    return HttpRequest.newBuilder()
      .uri(URI.create(baseUrl + path))
      .timeout(config.getRequestTimeout())
      .header("Authorization", "Bearer " + config.getApiKey())
      .header("Content-Type", "application/json")
      .header("Accept", "application/json");
  }

  /**
   * Builds and returns a POST request with a JSON body.
   *
   * @param path the relative path
   * @param body the object to serialize as JSON
   * @return the prepared HttpRequest
   * @throws ApiParseException if the body cannot be serialized to JSON
   */
  protected HttpRequest doPostJson(String path, Object body) {
    try {
      String json = mapper.writeValueAsString(body);
      return buildRequest(path)
        .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
        .build();
    } catch (JsonProcessingException ex) {
      String type = (body == null) ? "null" : body.getClass().getSimpleName();
      throw new ApiParseException("Request serialization error for " + type, ex);
    }
  }

  /**
   * Builds a POST request intended for streaming responses and optionally logs
   * the raw request.
   * <p>
   * Sets Accept header to text/event-stream.
   *
   * @param path the relative path
   * @param body the object to serialize as JSON
   * @return the prepared HttpRequest
   * @throws ApiParseException if the body cannot be serialized
   */
  protected HttpRequest doPostJsonStream(String path, Object body) {
    try {
      String json = mapper.writeValueAsString(body);
      return buildRequest(path)
        .setHeader("Accept", "text/event-stream")
        .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
        .build();
    } catch (JsonProcessingException ex) {
      String type = (body == null) ? "null" : body.getClass().getSimpleName();
      throw new ApiParseException("Request serialization error for " + type, ex);
    }
  }

  /**
   * Initiates an asynchronous streaming request and returns a handle for
   * controlling the stream.
   * <p>
   * The provided listener will be invoked on a background thread as data
   * arrives.
   *
   * @param request  the prepared streaming request
   * @param listener the listener to receive stream events
   * @return a handle that can be used to stop or await completion of the stream
   */
  protected ResponseStreamHandle sendStreaming(HttpRequest request, ResponseStreamListener listener) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(listener, "listener");
    ResponseStreamHandleImpl handle = new ResponseStreamHandleImpl(listener);
    long start = System.currentTimeMillis();
    CompletableFuture<HttpResponse<InputStream>> future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream());
    handle.attachFuture(future);
    future.whenComplete((response, error) -> {
      if (handle.isStopped()) {
        return;
      }
      if (error != null) {
        handle.fail(error);
        return;
      }
      int status = response.statusCode();
      InputStream body = response.body();
      if (status < 200 || status >= 300) {
        String bodyText = readQuietly(body);
        handle.fail(new ApiHttpException("API error {status=" + status + ", body=" + bodyText + "}"));
        return;
      }
      handle.attachBody(body);
      handle.readLoop(mapper);
      long time = System.currentTimeMillis() - start;
      if (handle.completedSuccessfully()) {
        LOG.log(Level.INFO, "STREAM ok '{'time={0} ms'}'", time);
      }
    });
    return handle;
  }

  // DEVELOPER NOTE: readQuietly is deliberately lenient. It is only used for error reporting
  // on non-success streaming responses where we want to include body text if possible.
  private static String readQuietly(InputStream body) {
    if (body == null) {
      return "";
    }
    try (InputStream in = body) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException ex) {
      return "";
    }
  }

  /**
   * Builds a simple GET request for the given path.
   *
   * @param path the relative path
   * @return the prepared HttpRequest
   */
  protected HttpRequest doGet(String path) {
    return buildRequest(path).GET().build();
  }

  /**
   * Builds a DELETE request for the given path.
   *
   * @param path the relative path
   * @return the prepared HttpRequest
   */
  protected HttpRequest doDelete(String path) {
    return buildRequest(path).DELETE().build();
  }

  /**
   * Builds a URL query string from a parameter object using reflection.
   * <p>
   * Only non-null fields are included. Collections are joined with commas.
   * Enums use their toString() value.
   *
   * @param params the object whose fields should become query parameters
   * @return a query string starting with "?" or an empty string if no
   *         parameters
   */
  protected String buildQueryString(Object params) {
    if (params == null) {
      return "";
    }

    // DEVELOPER NOTE: Reflection is used here to avoid forcing every parameter class
    // to implement a toMap() method. This keeps parameter objects simple POJOs.
    // Fields are made accessible regardless of visibility.
    Map<String, String> queryParams = new LinkedHashMap<>();

    Field[] fields = params.getClass().getDeclaredFields();
    for (Field field : fields) {
      field.setAccessible(true);
      try {
        Object value = field.get(params);
        if (value == null) {
          continue;
        }

        String key = field.getName();
        String valueStr;

        if (value instanceof Collection) {
          Collection<?> coll = (Collection<?>) value;
          valueStr = coll.stream()
            .filter(Objects::nonNull)
            .map(Object::toString)
            .collect(Collectors.joining(","));
        } else if (value.getClass().isEnum()) {
          valueStr = value.toString();
        } else {
          valueStr = value.toString();
        }

        if (!valueStr.isEmpty()) {
          queryParams.put(key, valueStr);
        }
      } catch (IllegalAccessException e) {
        // DEVELOPER NOTE: Silently ignore inaccessible fields. This can occur with
        // synthetic or security-manager restricted fields. Production usage rarely hits this.
      }
    }

    if (queryParams.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder("?");
    boolean first = true;
    for (Map.Entry<String, String> entry : queryParams.entrySet()) {
      if (!first) {
        sb.append("&");
      }
      first = false;

      sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
      sb.append("=");
      sb.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
    }

    return sb.toString();
  }

  /**
   * Performs a GET request with automatic retry and exponential backoff.
   *
   * @param path               the relative path
   * @param responseType       target response class
   * @param maxRetries         maximum number of retry attempts
   * @param initialDelayMillis delay before first retry
   * @param backoffMultiplier  multiplier applied to delay after each attempt
   * @param <T>                response type
   * @return the deserialized response
   */
  protected <T> T retryGet(String path, Class<T> responseType, int maxRetries, long initialDelayMillis, double backoffMultiplier) {
    if (maxRetries < 0) {
      throw new IllegalArgumentException("maxRetries cannot be negative");
    }
    if (backoffMultiplier <= 0) {
      throw new IllegalArgumentException("backoffMultiplier must be positive");
    }
    HttpRequest request = doGet(path);
    return sendRequestWithRetry(() -> request, responseType, maxRetries, initialDelayMillis, backoffMultiplier);
  }

  /**
   * Performs a POST request with automatic retry and exponential backoff.
   *
   * @param path               the relative path
   * @param body               request body to serialize
   * @param responseType       target response class
   * @param maxRetries         maximum number of retry attempts
   * @param initialDelayMillis delay before first retry
   * @param backoffMultiplier  multiplier applied to delay after each attempt
   * @param <T>                response type
   * @return the deserialized response
   */
  protected <T> T retryPost(String path, Object body, Class<T> responseType, int maxRetries, long initialDelayMillis, double backoffMultiplier) {
    if (maxRetries < 0) {
      throw new IllegalArgumentException("maxRetries cannot be negative");
    }
    if (backoffMultiplier <= 0) {
      throw new IllegalArgumentException("backoffMultiplier must be positive");
    }
    HttpRequest request = doPostJson(path, body);
    return sendRequestWithRetry(() -> request, responseType, maxRetries, initialDelayMillis, backoffMultiplier);
  }

  /**
   * Core retry implementation for class-based responses.
   * <p>
   * Retries on IOException/InterruptedException and on 5xx errors returned as
   * ApiHttpException. Other errors are not retried.
   */
  protected <T> T sendRequestWithRetry(Supplier<HttpRequest> requestSupplier, Class<T> responseType, int maxRetries, long initialDelayMillis, double backoffMultiplier) {
    long delay = initialDelayMillis;
    int attempt = 0;
    while (true) {
      try {
        HttpRequest request = requestSupplier.get();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return handleResponse(response, responseType);
      } catch (IOException | InterruptedException ex) {
        attempt++;
        if (attempt > maxRetries) {
          String clazz = (responseType == null) ? "null" : responseType.getSimpleName();
          throw new ApiHttpException("API request error after retries: " + requestSupplier.get().uri() + " " + clazz, ex);
        }
        try {
          TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new ApiHttpException("Interrupted during retry delay", ie);
        }
        delay = (long) (delay * backoffMultiplier);
      } catch (ApiHttpException ex) {
        // DEVELOPER NOTE: Only 5xx errors are retried. The check uses string containment
        // because the exact error message format may vary. This is intentional for simplicity.
        if (ex.getMessage().contains("API error: 5")) {
          attempt++;
          if (attempt > maxRetries) {
            throw ex;
          }
          try {
            TimeUnit.MILLISECONDS.sleep(delay);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ApiHttpException("Interrupted during retry delay", ie);
          }
          delay = (long) (delay * backoffMultiplier);
        } else {
          throw ex;
        }
      }
    }
  }

  /**
   * Core retry implementation for TypeReference-based responses.
   */
  protected <T> T sendRequestWithRetry(Supplier<HttpRequest> requestSupplier, TypeReference<T> responseType, int maxRetries, long initialDelayMillis, double backoffMultiplier) {
    long delay = initialDelayMillis;
    int attempt = 0;
    while (true) {
      try {
        HttpRequest request = requestSupplier.get();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return handleResponse(response, responseType);
      } catch (IOException | InterruptedException ex) {
        attempt++;
        if (attempt > maxRetries) {
          String type = (responseType == null) ? "null" : responseType.getType().getTypeName();
          throw new ApiHttpException("API request error after retries: " + requestSupplier.get().uri() + " " + type, ex);
        }
        try {
          TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw new ApiHttpException("Interrupted during retry delay", ie);
        }
        delay = (long) (delay * backoffMultiplier);
      } catch (ApiHttpException ex) {
        // Note: handleResponse for TypeReference throws IOException for errors, so this might not trigger, but kept for consistency
        if (ex.getMessage().contains("API error: 5")) {
          attempt++;
          if (attempt > maxRetries) {
            throw ex;
          }
          try {
            TimeUnit.MILLISECONDS.sleep(delay);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ApiHttpException("Interrupted during retry delay", ie);
          }
          delay = (long) (delay * backoffMultiplier);
        } else {
          throw ex;
        }
      }
    }
  }

}
