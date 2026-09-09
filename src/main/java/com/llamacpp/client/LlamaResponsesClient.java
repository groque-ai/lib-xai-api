package com.llamacpp.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.llamacpp.client.model.ModelListResponse;
import com.llamacpp.client.util.LlamaRequestTransformer;
import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.client.ResponseStreamHandle;
import com.xai.client.ResponseStreamListener;
import com.xai.client.exception.ApiHttpException;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * llama.cpp client for Responses plus local discovery. One server, one type.
 *
 * @author Key Bridge
 * @since v1.2.0 created 2026-09-09
 */
public class LlamaResponsesClient extends LlamaAbstractClient {

  public LlamaResponsesClient() {
    this(LlamaClientConfig.readConfig());
  }

  public LlamaResponsesClient(LlamaClientConfig config) {
    super(config);
  }

  public ModelResponse generate(ModelRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("request");
    }
    if (Boolean.TRUE.equals(request.getStream())) {
      throw new IllegalArgumentException("stream=true requires generateStreaming");
    }
    LlamaRequestTransformer.apply(request);
    return sendRequest(doPostJson("/v1/responses", request), ModelResponse.class);
  }

  public ResponseStreamHandle generateStreaming(ModelRequest request, ResponseStreamListener listener) {
    if (request == null) {
      throw new IllegalArgumentException("request");
    }
    if (listener == null) {
      throw new IllegalArgumentException("listener");
    }
    request.setStream(Boolean.TRUE);
    LlamaRequestTransformer.apply(request);
    return sendStreaming(doPostJsonStream("/v1/responses", request), listener);
  }

  public ModelListResponse getModels() {
    return sendRequest(doGet("/models"), ModelListResponse.class);
  }

  /**
   * GET /health. 200 and status=ok is true; any other HTTP status is false;
   * transport failure throws.
   */
  public boolean isHealthy() {
    HttpRequest httpRequest = doGet("/health");
    try {
      HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        return false;
      }
      JsonNode node = mapper.readTree(response.body());
      return node != null && "ok".equals(node.path("status").asText());
    } catch (IOException | InterruptedException ex) {
      if (ex instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      throw new ApiHttpException("API request error: " + httpRequest.uri() + " health", ex);
    }
  }
}
