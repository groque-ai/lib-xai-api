package com.xai.client;

import com.xai.api.responses.ModelRequest;
import com.xai.api.responses.ModelResponse;
import com.xai.api.responses.admin.DeleteStoredCompletionResponse;
import java.net.http.HttpRequest;

/**
 * Client for interacting with XAI responses API endpoints.
 * <p>
 * This class provides methods to generate, retrieve, and delete model responses
 * via HTTP requests. It extends {@link XaiAbstractClient} to reuse common HTTP
 * client functionality and resource management.
 * <p>
 * Thread-safety: This class is not thread-safe due to potential mutable state
 * in the superclass. Instances should not be shared across multiple threads
 * without external synchronization.
 * <p>
 * Resource management: HTTP requests are handled by the superclass, which
 * ensures explicit closure of resources such as HTTP connections to prevent
 * leaks.
 * <p>
 * Usage notes: Ensure the superclass is properly configured with an HTTP client
 * before using this class. The {@code sendRequest} method in the superclass
 * returns {@code null} for HTTP 404 responses, which is propagated by methods
 * in this class.
 */
public class XaiResponsesClient extends XaiAbstractClient {

  private static final String RESPONSES_PATH = "/responses";

  /**
   * Constructs a new {@code XaiResponsesClient} with the default responses API
   * path.
   * <p>
   * This constructor initializes the client with the predefined path for
   * responses endpoints.
   */
  public XaiResponsesClient() {
    super(RESPONSES_PATH);
  }

  /**
   * Generates a new model response based on the provided request.
   * <p>
   * This method constructs a POST request with the request object serialized as
   * JSON and sends it to the responses endpoint.
   * <p>
   * @param request the model request containing parameters for generation; must
   *                not be {@code null}
   * @return the generated model response, or {@code null} if the HTTP response
   *         indicates a 404 error (as handled by {@code sendRequest})
   * @throws IllegalArgumentException if {@code request} is {@code null}
   * @throws RuntimeException         if the HTTP request fails, the response
   *                                  cannot be parsed, or other network issues
   *                                  occur
   */
  public ModelResponse generate(ModelRequest request) {
    HttpRequest httpRequest = doPostJson("", request);
    return sendRequest(httpRequest, ModelResponse.class);
  }

  /**
   * Retrieves a model response by its unique identifier.
   * <p>
   * This method constructs a GET request to the specific response endpoint
   * identified by the provided ID.
   * <p>
   * @param responseId the unique identifier of the response; must not be
   *                   {@code null} or empty
   * @return the model response associated with the ID, or {@code null} if the
   *         response is not found (HTTP 404, as handled by {@code sendRequest})
   * @throws IllegalArgumentException if {@code responseId} is {@code null} or
   *                                  empty
   * @throws RuntimeException         if the HTTP request fails, the response
   *                                  cannot be parsed, or other network issues
   *                                  occur
   */
  public ModelResponse get(String responseId) {
    HttpRequest request = super.doGet("/" + responseId);
    return sendRequest(request, ModelResponse.class);
  }

  /**
   * Deletes a stored completion response by its unique identifier.
   * <p>
   * This method constructs a DELETE request to the specific response endpoint
   * identified by the provided ID.
   * <p>
   * @param responseId the unique identifier of the response; must not be
   *                   {@code null} or empty
   * @return the delete response, or {@code null} if the response is not found
   *         (HTTP 404, as handled by {@code sendRequest})
   * @throws IllegalArgumentException if {@code responseId} is {@code null} or
   *                                  empty
   * @throws RuntimeException         if the HTTP request fails, the response
   *                                  cannot be parsed, or other network issues
   *                                  occur
   */
  public DeleteStoredCompletionResponse delete(String responseId) {
    HttpRequest request = super.doDelete("/" + responseId);
    return sendRequest(request, DeleteStoredCompletionResponse.class);
  }

}
