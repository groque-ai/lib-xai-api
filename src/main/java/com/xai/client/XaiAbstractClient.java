package com.xai.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.xai.client.exception.ApiHttpException;
import com.xai.client.exception.ApiParseException;
import java.io.IOException;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Abstract base implementation for API services providing HTTP client
 * functionality with retry and backoff support.
 * <p>
 * This class encapsulates the HTTP client, JSON mapper, and request handling
 * logic.
 * <p>
 * Concurrency assumptions: This class is thread-safe for concurrent invocations
 * of its methods, as the HttpClient is thread-safe and no mutable shared state
 * is accessed without synchronization. The ObjectMapper instance is shared
 * across threads; it is assumed to be used in a read-only manner without
 * configuration changes during runtime. Resource management: HttpClient is
 * built once and reused; no explicit closing is required as it manages its own
 * resources. If subclasses or users need to close resources, they must handle
 * it externally.
 */
abstract class XaiAbstractClient implements AutoCloseable {

  /**
   * The base URI for the API.
   */
  private static final String BASE_URI = "https://api.x.ai/v1";
  private static final Logger LOG = Logger.getLogger(XaiAbstractClient.class.getName());

  /**
   * The client configuration, loaded from external sources.
   */
  protected final XaiClientConfig config;

  /**
   * The HTTP client used for sending requests.
   * <p>
   * Thread-safe and reused across requests.
   */
  protected final HttpClient httpClient;

  /**
   * The base URL for this service.
   */
  protected final String baseUrl;

  /**
   * The JSON mapper for serialization and deserialization.
   * <p>
   * Assumed to be used in a thread-safe manner (read-only operations).
   */
  protected final ObjectMapper mapper;

  /**
   * Constructs a new AbstractServiceImpl with the specified path.
   * <p>
   * Initializes the HTTP client with configuration from XaiClientConfig, sets
   * up the base URL, and creates a shared ObjectMapper.
   *
   * @param path the service-specific path to append to the base URI
   * @throws IllegalStateException if configuration loading fails
   */
  protected XaiAbstractClient(String path) {
    config = XaiClientConfig.readConfig();
    this.baseUrl = BASE_URI + path;

    this.httpClient = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .connectTimeout(config.getConnectTimeout())
      .followRedirects(config.isFollowRedirects()
                       ? HttpClient.Redirect.NORMAL
                       : HttpClient.Redirect.NEVER)
      .build();

    this.mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
      //      .enable(SerializationFeature.INDENT_OUTPUT)
      .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
//      .setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  /**
   * AutoCloseable implementation. Closes this resource, relinquishing any
   * underlying resources.
   *
   * @throws Exception if this resource cannot be closed
   */
  @Override
  public void close() throws Exception {
    // no op
  }

  /**
   * Sends an HTTP request and handles the response, deserializing to the
   * specified type.
   * <p>
   * This method does not include retry logic; use sendRequestWithRetry for
   * that.
   *
   * @param <T>          the type of the response
   * @param request      the HTTP request to send
   * @param responseType the class of the response type
   * @return the deserialized response
   * @throws ApiHttpException  if an HTTP error occurs
   * @throws ApiParseException if JSON parsing fails
   */
  protected <T> T sendRequest(HttpRequest request, Class<T> responseType) {
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return handleResponse(response, responseType); // safely intercepts 404
    } catch (IOException | InterruptedException ex) {
      String clazz = (responseType == null) ? "null" : responseType.getSimpleName();
      throw new ApiHttpException("API request error: " + request.uri() + " " + clazz, ex);
    }
  }

  /**
   * Sends an HTTP request and handles the response, deserializing to the
   * specified type reference.
   * <p>
   * This method does not include retry logic; use sendRequestWithRetry for
   * that.
   *
   * @param <T>          the type of the response
   * @param request      the HTTP request to send
   * @param responseType the type reference for the response
   * @return the deserialized response
   * @throws ApiHttpException  if an HTTP error occurs
   * @throws ApiParseException if JSON parsing fails
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
   * Handles the HTTP response for Class-based deserialization.
   *
   * @param <T>      the type of the response
   * @param response the HTTP response
   * @param type     the class to deserialize to
   * @return the deserialized object, or null for Void
   * @throws ApiHttpException  for non-2xx status codes
   * @throws ApiParseException for JSON processing errors
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
        // HTTP/1.1 404 Not Found
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
   * Handles the HTTP response for TypeReference-based deserialization.
   *
   * @param <T>      the type of the response
   * @param response the HTTP response
   * @param type     the type reference to deserialize to
   * @return the deserialized object
   * @throws IOException for non-2xx status codes or JSON processing errors
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
   * Creates a base HTTP request builder with common headers.
   *
   * @param path the endpoint path
   * @return the request builder
   */
  protected HttpRequest.Builder baseRequest(String path) {
    return HttpRequest.newBuilder()
      .uri(URI.create(baseUrl + path))
      .header("Authorization", "Bearer " + config.getApiKey())
      .header("Content-Type", "application/json")
      .header("Accept", "application/json");
  }

  /**
   * Creates a POST request with JSON body.
   *
   * @param path the endpoint path
   * @param body the object to serialize as JSON
   * @return the HTTP request
   * @throws ApiParseException if serialization fails
   */
  protected HttpRequest doPostJson(String path, Object body) {
    try {
      String json = mapper.writeValueAsString(body);
      return baseRequest(path)
        .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
        .build();
    } catch (JsonProcessingException ex) {
      String type = (body == null) ? "null" : body.getClass().getSimpleName();
      throw new ApiParseException("Request serialization error for " + type, ex);
    }
  }

  /**
   * Creates a GET request.
   *
   * @param path the endpoint path
   * @return the HTTP request
   */
  protected HttpRequest doGet(String path) {
    return baseRequest(path).GET().build();
  }

  /**
   * Creates a DELETE request.
   *
   * @param path the endpoint path
   * @return the HTTP request
   */
  protected HttpRequest doDelete(String path) {
    return baseRequest(path).DELETE().build();
  }

  /**
   * Builds a query string from the fields of the given object.
   *
   * @param params the object whose fields are used as query parameters
   * @return the encoded query string, or empty if no parameters
   */
  protected String buildQueryString(Object params) {
    if (params == null) {
      return "";
    }

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
        // ignore or log if needed
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
   * Sends a GET request with retry and exponential backoff.
   * <p>
   * Retries on IOException, InterruptedException, or HTTP status codes 500-599.
   *
   * @param <T>                the response type
   * @param path               the endpoint path
   * @param responseType       the class of the response type
   * @param maxRetries         the maximum number of retries (0 means no retry)
   * @param initialDelayMillis the initial delay in milliseconds
   * @param backoffMultiplier  the multiplier for exponential backoff (e.g.,
   *                           2.0)
   * @return the deserialized response
   * @throws ApiHttpException         if all retries fail with HTTP errors
   * @throws ApiParseException        if JSON parsing fails
   * @throws IllegalArgumentException if maxRetries is negative or
   *                                  backoffMultiplier is <= 0
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
   * Sends a POST request with retry and exponential backoff.
   * <p>
   * Retries on IOException, InterruptedException, or HTTP status codes 500-599.
   *
   * @param <T>                the response type
   * @param path               the endpoint path
   * @param body               the request body object
   * @param responseType       the class of the response type
   * @param maxRetries         the maximum number of retries (0 means no retry)
   * @param initialDelayMillis the initial delay in milliseconds
   * @param backoffMultiplier  the multiplier for exponential backoff (e.g.,
   *                           2.0)
   * @return the deserialized response
   * @throws ApiHttpException         if all retries fail with HTTP errors
   * @throws ApiParseException        if JSON parsing fails
   * @throws IllegalArgumentException if maxRetries is negative or
   *                                  backoffMultiplier is <= 0
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
   * Sends an HTTP request with retry and exponential backoff.
   * <p>
   * Retries on IOException, InterruptedException, or HTTP status codes 500-599.
   *
   * @param <T>                the response type
   * @param requestSupplier    supplier for the HTTP request (to allow
   *                           recreation if needed)
   * @param responseType       the class of the response type
   * @param maxRetries         the maximum number of retries (0 means no retry)
   * @param initialDelayMillis the initial delay in milliseconds
   * @param backoffMultiplier  the multiplier for exponential backoff (e.g.,
   *                           2.0)
   * @return the deserialized response
   * @throws ApiHttpException  if all retries fail with HTTP errors
   * @throws ApiParseException if JSON parsing fails
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
        // Check if status is retryable (5xx)
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
   * Sends an HTTP request with retry and exponential backoff for TypeReference.
   * <p>
   * Retries on IOException, InterruptedException, or HTTP status codes 500-599.
   *
   * @param <T>                the response type
   * @param requestSupplier    supplier for the HTTP request
   * @param responseType       the type reference for the response
   * @param maxRetries         the maximum number of retries (0 means no retry)
   * @param initialDelayMillis the initial delay in milliseconds
   * @param backoffMultiplier  the multiplier for exponential backoff (e.g.,
   *                           2.0)
   * @return the deserialized response
   * @throws ApiHttpException  if all retries fail with HTTP errors
   * @throws ApiParseException if JSON parsing fails
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
