package com.xai.client;

import com.xai.api.tokenize.TokenizeRequest;
import com.xai.api.tokenize.TokenizeResponse;
import java.net.http.HttpRequest;

/**
 * Client for tokenizing text using the XAI API.
 * <p>
 * This class extends {@link XaiAbstractClient} to provide functionality for
 * text tokenization. It sends requests to the tokenize-text endpoint and
 * processes responses.
 * <p>
 * Concurrency: Instances of this class are not thread-safe. Each instance
 * should be used by a single thread. If concurrent access is needed, external
 * synchronization is required.
 * <p>
 * Usage example:
 * <pre>
 * XaiTokenizeClient client = new XaiTokenizeClient();
 * TokenizeRequest request = new TokenizeRequest();
 * TokenizeResponse response = client.createTokenizedText(request);
 * </pre>
 *
 * @author Key Bridge
 */
public class XaiTokenizeClient extends XaiAbstractClient {

  private static final String TOKENIZE_PATH = "/tokenize-text";

  /**
   * Constructs a new {@code XaiTokenizeClient}.
   * <p>
   * Initializes the client with the tokenize endpoint path.
   */
  public XaiTokenizeClient() {
    super(TOKENIZE_PATH);
  }

  /**
   * Creates tokenized text based on the provided request.
   * <p>
   * This method performs a POST request to the tokenize-text API endpoint using
   * the JSON representation of the request. The response is parsed into a
   * {@link TokenizeResponse} object.
   * <p>
   * The request parameter must not be null. Passing a null value may result in
   * a {@link NullPointerException}.
   * <p>
   * This method is not thread-safe and should not be called concurrently from
   * multiple threads on the same instance without external synchronization.
   *
   * @param request the tokenization request containing the text and model
   *                information; must not be null
   * @return the tokenization response containing the tokenized text
   * @throws NullPointerException if the request is null
   */
  public TokenizeResponse createTokenizedText(TokenizeRequest request) {
    HttpRequest httpRequest = doPostJson("", request);
    return sendRequest(httpRequest, TokenizeResponse.class);
  }
}
