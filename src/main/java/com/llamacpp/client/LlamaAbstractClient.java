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
 * HTTP/JSON/SSE transport for llama.cpp. Authorization is omitted unless the
 * config has an API key.
 *
 * @author Key Bridge
 * @since v1.2.0 created 2026-09-09
 */
public abstract class LlamaAbstractClient implements AutoCloseable {

  private static final String DEFAULT_BASE_URL = "http://127.0.0.1:8080";
  private static final Logger LOG = Logger.getLogger(LlamaAbstractClient.class.getName());

  protected final LlamaClientConfig config;
  protected final HttpClient httpClient;
  protected final ObjectMapper mapper;
  protected final String baseUrl;

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

    // llama.cpp httplib is HTTP/1.1
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

  @Override
  public void close() throws Exception {
    // HttpClient is shared and not closed
  }

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

  protected HttpRequest doGet(String path) {
    return buildRequest(path).GET().build();
  }

  protected <T> T sendRequest(HttpRequest request, Class<T> responseType) {
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      return handleResponse(response, responseType);
    } catch (IOException | InterruptedException ex) {
      String clazz = (responseType == null) ? "null" : responseType.getSimpleName();
      throw new ApiHttpException("API request error: " + request.uri() + " " + clazz, ex);
    }
  }

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
