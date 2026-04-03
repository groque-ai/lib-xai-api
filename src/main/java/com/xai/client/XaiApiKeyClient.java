package com.xai.client;

import com.xai.api.auth.ApiKey;
import java.net.http.HttpRequest;

/**
 * A client for interacting with the XAI API key service.
 * <p>
 * This class extends {@link XaiAbstractClient} and provides methods to retrieve
 * API keys.
 * <p>
 * This class is thread-safe, as it does not maintain mutable state and relies
 * on the thread-safety of its parent class.
 *
 * @author Key Bridge
 */
public class XaiApiKeyClient extends XaiAbstractClient {

  /**
   * The path segment used for API key operations.
   */
  private static final String API_KEY_PATH = "/api-key";

  /**
   * Constructs a new {@code XaiApiKeyClient} with the default API key path.
   * <p>
   * This constructor initializes the client with the predefined API key path
   * segment.
   */
  public XaiApiKeyClient() {
    super(API_KEY_PATH);
  }

  /**
   * Retrieves the API key from the service.
   * <p>
   * This method sends a GET request to the API key endpoint and returns the
   * response as an {@link ApiKey} object.
   * <p>
   * Note: This method may throw unchecked exceptions related to HTTP
   * operations, such as network failures.
   *
   * @return the API key retrieved from the service
   */
  public ApiKey getApiKey() {
    HttpRequest request = doGet("");
    return sendRequest(request, ApiKey.class);
  }

}
