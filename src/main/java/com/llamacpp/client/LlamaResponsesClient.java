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
 * Client implementation for the Llama responses API providing synchronous and
 * streaming response generation along with model discovery and health
 * verification.
 * <p>
 * Extends LlamaAbstractClient to reuse shared HTTP client infrastructure,
 * request builders, and response handling while adding responses-specific
 * endpoints and behavior.
 * <p>
 * Design decision: Stream mode is explicitly toggled on the request object
 * before transformation and transmission to ensure the server returns either a
 * complete payload or chunked data as required.
 *
 * @since 1.1.0 created 2026-09-09
 */
public class LlamaResponsesClient extends LlamaAbstractClient {

  /**
   * Creates a new LlamaResponsesClient instance using configuration loaded via
   * LlamaClientConfig.readConfig().
   * <p>
   * Delegates immediately to the primary constructor to ensure consistent
   * initialization path through the superclass.
   */
  public LlamaResponsesClient() {
    this(LlamaClientConfig.readConfig());
  }

  /**
   * <p>
   * Creates a new LlamaResponsesClient with the supplied configuration.
   * <p>
   * Delegates configuration and HTTP client construction to the superclass
   * LlamaAbstractClient.
   *
   * @param config the LlamaClientConfig to use for endpoint, timeouts, and
   *               authentication settings
   */
  public LlamaResponsesClient(LlamaClientConfig config) {
    super(config);
  }

  /**
   * Generates a complete, non-streaming response for the provided model
   * request.
   * <p>
   * Configures the request for non-streaming execution, applies preprocessing
   * via LlamaRequestTransformer, and posts the payload to the /v1/responses
   * endpoint.
   * <p>
   * The resulting response is deserialized directly into a ModelResponse object
   * using the inherited sendRequest mechanism.
   *
   * @param request the ModelRequest containing prompt, parameters, and model
   *                selection; must not be null
   * @return a fully populated ModelResponse containing the generated output and
   *         metadata
   * @throws IllegalArgumentException if request is null
   * @throws ApiHttpException         if the HTTP request fails or the response
   *                                  cannot be deserialized
   */
  public ModelResponse generate(ModelRequest request) {
    if (request == null) {
      throw new IllegalArgumentException("request");
    }
    request.setStream(Boolean.FALSE);
    // Developer note: LlamaRequestTransformer.apply() is invoked after stream flag configuration to allow transformers to inspect and potentially override stream-related settings or add model-specific defaults.
    LlamaRequestTransformer.apply(request);
    return sendRequest(doPostJson("/v1/responses", request), ModelResponse.class);
  }

  /**
   * <p>
   * Starts an asynchronous streaming response generation session for the given
   * request.
   * <p>
   * Sets the stream flag to true, applies request transformations, and
   * initiates a streaming POST to /v1/responses.
   * <p>
   * The returned ResponseStreamHandle manages the underlying connection and
   * delivers parsed events to the supplied listener.
   *
   * @param request  the ModelRequest to execute in streaming mode; must not be
   *                 null
   * @param listener the ResponseStreamListener that will receive incremental
   *                 response events; must not be null
   * @return a ResponseStreamHandle that can be used to cancel or monitor the
   *         active stream
   * @throws IllegalArgumentException if request or listener is null
   * @throws ApiHttpException         if the streaming connection cannot be
   *                                  established
   */
  public ResponseStreamHandle generateStreaming(ModelRequest request, ResponseStreamListener listener) {
    if (request == null) {
      throw new IllegalArgumentException("request");
    }
    if (listener == null) {
      throw new IllegalArgumentException("listener");
    }
    // Developer note: Setting stream to Boolean.TRUE instructs the server to deliver Server-Sent Events or chunked responses rather than a single JSON document.
    request.setStream(Boolean.TRUE);
    LlamaRequestTransformer.apply(request);
    return sendStreaming(doPostJsonStream("/v1/responses", request), listener);
  }

  /**
   * <p>
   * Retrieves the current list of available models from the Llama service.
   * <p>
   * Performs an unauthenticated-style GET against the /models endpoint and
   * deserializes the result into a ModelListResponse.
   *
   * @return ModelListResponse containing metadata for all models supported by
   *         the backend
   */
  public ModelListResponse getModels() {
    return sendRequest(doGet("/models"), ModelListResponse.class);
  }

  /**
   * Performs a lightweight health probe against the Llama service.
   * <p>
   * Issues a direct GET to /health, verifies HTTP status 200, and inspects the
   * JSON body for a top-level "status" field containing the value "ok".
   * <p>
   * Uses raw HttpClient.send() rather than the higher-level sendRequest helper
   * to permit inspection of non-success responses and custom status extraction
   * without triggering exception paths.
   *
   * @return true when the service returns HTTP 200 and JSON status equals "ok";
   *         false for any other successful or unsuccessful response
   * @throws ApiHttpException if an I/O failure or thread interruption prevents
   *                          completion of the health request
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
        // Developer note: Restoring the interrupted status flag is required after catching InterruptedException to preserve the thread's interrupt state for callers and higher-level frameworks.
        Thread.currentThread().interrupt();
      }
      throw new ApiHttpException("API request error: " + httpRequest.uri() + " health", ex);
    }
  }
}
