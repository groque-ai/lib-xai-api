package com.llamacpp.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.xai.client.ResponseStreamHandle;
import com.xai.client.ResponseStreamHandleImpl;
import com.xai.client.ResponseStreamListener;
import com.xai.client.exception.ApiHttpException;
import com.xai.client.exception.ApiParseException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract base implementation for Llama CPP model server HTTP clients.
 * <p>
 * Provides shared infrastructure for building and executing HTTP requests, JSON
 * serialization/deserialization via Jackson, synchronous request handling, and
 * asynchronous streaming response support. Subclasses extend this class to
 * implement specific API endpoints (e.g. completions, chat, embeddings).
 * <p>
 * The design centralizes HTTP client lifecycle management, timeout
 * configuration, header handling (including optional Bearer token
 * authorization), and consistent error mapping to {@link ApiHttpException} and
 * {@link ApiParseException}.
 * <p>
 * Usage notes: Clients are intended to be created once and reused. Call
 * {@link #close()} when the client is no longer needed (currently a no-op but
 * required by the {@link AutoCloseable} contract). The underlying
 * {@link HttpClient} and {@link ObjectMapper} are thread-safe, allowing
 * concurrent use from multiple threads. Streaming operations are fully
 * asynchronous and report progress via the supplied
 * {@link ResponseStreamListener}.
 * <p>
 * Developer note: This abstract class follows a template method style where
 * protected helper methods (buildRequest, doPostJson, sendRequest, etc.) are
 * composed by concrete subclasses. The 404 special-case behavior in response
 * handling is preserved for backward compatibility with optional resource APIs.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-09
 */
public abstract class LlamaAbstractClient implements AutoCloseable {

  /**
   * Default base URL applied when {@link LlamaClientConfig#getBaseUrl()}
   * returns null or blank.
   * <p>
   * The value is normalized (trailing slash removed) during construction.
   */
  private static final String DEFAULT_BASE_URL = "http://127.0.0.1:8080";

  /**
   * Class-level logger used for informational messages and non-fatal error
   * reporting (e.g. 404 responses and successful stream completion timing).
   */
  private static final Logger LOG = Logger.getLogger(LlamaAbstractClient.class.getName());

  /**
   * Immutable client configuration containing timeouts, API key, base URL, and
   * redirect policy.
   * <p>
   * Stored as a final field to guarantee consistent behavior for the lifetime
   * of the client instance.
   */
  protected final LlamaClientConfig config;

  /**
   * Pre-configured {@link HttpClient} instance used for all synchronous and
   * asynchronous HTTP operations.
   * <p>
   * Created with HTTP/1.1, the connect timeout from config, and the redirect
   * policy derived from {@link LlamaClientConfig#isFollowRedirects()}.
   * <p>
   * Developer note: HttpClient is thread-safe and connection pooling is managed
   * internally by the JDK implementation.
   */
  protected final HttpClient httpClient;

  /**
   * Shared Jackson {@link ObjectMapper} configured for this client's
   * serialization and deserialization requirements.
   * <p>
   * Settings include: NON_NULL inclusion, failure on unknown properties
   * disabled, unknown enum values read as null, map entry ordering enabled, and
   * transient marker propagation enabled.
   * <p>
   * Developer note: A single mapper instance is reused for performance and to
   * guarantee consistent serialization behavior across all requests.
   */
  protected final ObjectMapper mapper;

  /**
   * Normalized base URL (no trailing slash) used as the prefix for all request
   * URIs.
   * <p>
   * Derived from {@link LlamaClientConfig#getBaseUrl()} or
   * {@link #DEFAULT_BASE_URL} during construction.
   */
  protected final String baseUrl;

  /**
   * Constructs a new abstract client using the supplied configuration.
   * <p>
   * The base URL is normalized by stripping any trailing slash. If no base URL
   * is supplied, {@link #DEFAULT_BASE_URL} is used. The internal
   * {@link HttpClient} and {@link ObjectMapper} are initialized exactly once.
   * <p>
   * Developer note: Constructor performs eager initialization of expensive
   * objects (HttpClient and ObjectMapper) to avoid repeated setup cost on every
   * request. Null checks are performed via Objects.requireNonNull.
   *
   * @param config the client configuration; must not be null
   * @throws NullPointerException if config is null
   */
  protected LlamaAbstractClient(LlamaClientConfig config) {
    this.config = Objects.requireNonNull(config, "config");
    String root = config.getBaseUrl();
    if (root == null || root.isBlank()) {
      root = DEFAULT_BASE_URL;
    }
    if (root.endsWith("/")) {
      root = root.substring(0, root.length() - 1);
    }
    this.baseUrl = root;

    // Developer note: Base URL normalization ensures consistent path
    // concatenation in buildRequest without producing malformed URIs
    // containing double slashes.
    this.httpClient = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_1_1)
      .connectTimeout(config.getConnectTimeout())
      .followRedirects(config.isFollowRedirects()
                       ? HttpClient.Redirect.NORMAL
                       : HttpClient.Redirect.NEVER)
      .build();

    this.mapper = new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
      .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
      .enable(MapperFeature.PROPAGATE_TRANSIENT_MARKER);
  }

  /**
   * Closes this client and releases any associated resources.
   * <p>
   * Current implementation is a no-op because the JDK {@link HttpClient}
   * manages its own connection pool and does not require explicit shutdown for
   * typical usage. The method exists solely to fulfill the
   * {@link AutoCloseable} contract.
   * <p>
   * Developer note: Subclasses that allocate additional resources (custom
   * executors, file handles, etc.) should override this method and invoke
   * super.close() after performing their own cleanup. No resources are leaked
   * by the base implementation.
   *
   * @throws Exception if an error occurs while closing
   */
  @Override
  public void close() throws Exception {
    // Developer note: Explicitly empty to satisfy AutoCloseable without
    // forcing unnecessary shutdown semantics on the shared HttpClient.
  }

  /**
   * Creates a new {@link HttpRequest.Builder} pre-populated with the target
   * URI, request timeout, and standard headers.
   * <p>
   * The URI is formed by appending the supplied path to {@link #baseUrl}.
   * Content-Type and Accept headers are set to application/json. When an API
   * key is present, the Authorization header is added using Bearer token
   * format.
   * <p>
   * Developer note: This method centralizes header and timeout logic so that
   * all request variants (GET, POST, streaming) remain consistent.
   *
   * @param path the API path segment (should start with '/')
   * @return a builder instance ready for method specification and build
   */
  protected HttpRequest.Builder buildRequest(String path) {
    HttpRequest.Builder builder = HttpRequest.newBuilder()
      .uri(URI.create(baseUrl + path))
      .timeout(config.getRequestTimeout())
      .header("Content-Type", "application/json")
      .header("Accept", "application/json");
    if (config.hasApiKey()) {
      builder.header("Authorization", "Bearer " + config.getApiKey());
    }
    return builder;
  }

  /**
   * Builds a POST {@link HttpRequest} with a JSON-serialized body.
   * <p>
   * The body object is converted to JSON using the configured {@link #mapper}.
   * The resulting request uses application/json content type.
   *
   * @param path the target API path
   * @param body the object to be serialized as the request body; may be null
   * @return a fully constructed POST request
   * @throws ApiParseException if JSON serialization of the body fails
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
   * Builds a POST {@link HttpRequest} intended for server-sent event streaming.
   * <p>
   * Identical to {@link #doPostJson(String, Object)} except that the Accept
   * header is overridden to "text/event-stream" to signal streaming intent to
   * the server.
   *
   * @param path the target API path
   * @param body the object to be serialized as the request body
   * @return a fully constructed streaming POST request
   * @throws ApiParseException if JSON serialization of the body fails
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
   * Builds a simple GET {@link HttpRequest} for the given path.
   * <p>
   * Uses the standard headers and timeout supplied by
   * {@link #buildRequest(String)}.
   *
   * @param path the target API path
   * @return a fully constructed GET request
   */
  protected HttpRequest doGet(String path) {
    return buildRequest(path).GET().build();
  }

  /**
   * Executes a synchronous HTTP request and returns the deserialized response.
   * <p>
   * Uses blocking
   * {@link HttpClient#send(HttpRequest, HttpResponse.BodyHandler)}. The
   * response is processed by {@link #handleResponse(HttpResponse, Class)}.
   *
   * @param request      the request to execute
   * @param responseType the expected response class; use Void.class for no body
   * @param <T>          the response type
   * @return the deserialized response object, or null for Void or 404 responses
   * @throws ApiHttpException if an I/O error, interruption, or non-success HTTP
   *                          status occurs
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
   * Processes an HTTP response, handling success, 404, and error cases.
   * <p>
   * For 2xx status codes the body is deserialized using the supplied type
   * (unless the type is Void.class). Status 404 is treated specially by logging
   * and returning null. All other non-success statuses result in an
   * {@link ApiHttpException}.
   * <p>
   * Developer note: The 404-to-null behavior is intentional to support optional
   * resource lookup patterns common in some Llama APIs, but callers must be
   * aware that null can mean "not found" rather than an empty result.
   *
   * @param response the raw HTTP response
   * @param type     the target deserialization class
   * @param <T>      the response type
   * @return deserialized object or null for Void/404 cases
   * @throws ApiParseException if the response body cannot be deserialized
   * @throws ApiHttpException  for non-success HTTP status codes
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
        LOG.log(Level.INFO, "{0} error (not found) '{'status={1}, body={2}'}'", new Object[]{this.getClass().getSimpleName(), status, body});
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
   * Initiates an asynchronous streaming request and returns a handle for
   * controlling and observing the stream.
   * <p>
   * The request is executed using {@link HttpClient#sendAsync}. The provided
   * {@link ResponseStreamListener} receives callbacks on a background thread.
   * The returned {@link ResponseStreamHandle} can be used to stop the stream
   * and query completion status.
   * <p>
   * Developer note: The CompletableFuture is attached to the handle before
   * registration of whenComplete to allow early cancellation. Timing
   * information is logged only on successful completion.
   *
   * @param request  the streaming request (typically created via
   *                 {@link #doPostJsonStream(String, Object)})
   * @param listener the listener that will receive stream events; must not be
   *                 null
   * @return a handle that can be used to stop or inspect the stream
   * @throws NullPointerException if request or listener is null
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

  /**
   * Safely reads the entire content of an InputStream as UTF-8 text.
   * <p>
   * Used exclusively for capturing error response bodies so that error
   * reporting does not itself throw additional exceptions. The stream is always
   * closed.
   * <p>
   * Developer note: This helper exists to prevent nested I/O exceptions during
   * failure paths. Empty string is returned on any error or null input.
   *
   * @param body the input stream to read; may be null
   * @return the body content as a string, or empty string on failure
   */
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
}
