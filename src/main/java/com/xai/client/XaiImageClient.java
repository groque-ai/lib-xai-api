package com.xai.client;

import com.xai.api.images.EditImageRequest;
import com.xai.api.images.GenerateImageRequest;
import com.xai.api.images.GeneratedImageResponse;
import java.net.http.HttpRequest;

/**
 * <p>
 * Client for XAI image generation and editing services.
 * <p>
 * This class provides methods to generate new images from text prompts and to
 * edit existing images.
 * <p>
 * Instances of this class are not thread-safe. Concurrent access must be
 * synchronized externally.
 * <p>
 * HTTP resources are properly closed to prevent leaks.
 *
 * @author Key Bridge
 */
public class XaiImageClient extends XaiAbstractClient {

  private static final String GENERATE_IMAGES_PATH = "/images/generations";
  private static final String EDIT_IMAGES_PATH = "/images/edits";

  /**
   * Constructs a new XaiImageClient instance.
   * <p>
   * The client is initialized with an empty API key, which must be set via the
   * parent class for authentication.
   */
  public XaiImageClient() {
    super("");
  }

  /**
   * <p>
   * Generates images based on the specified request.
   * <p>
   * Sends a POST request to the XAI image generation API with the provided
   * parameters and returns the generated images.
   * <p>
   * This method is not thread-safe and should not be called concurrently on the
   * same instance.
   *
   * @param request the request object containing generation parameters such as
   *                prompt, size, and count
   * @return a response object containing the generated images
   * @throws RuntimeException if the API request fails or the response is
   *                          invalid
   */
  public GeneratedImageResponse createImages(GenerateImageRequest request) {
    HttpRequest httpRequest = doPostJson(GENERATE_IMAGES_PATH, request);
    return sendRequest(httpRequest, GeneratedImageResponse.class);
  }

  /**
   * <p>
   * Edits an existing image based on the specified request.
   * <p>
   * Sends a POST request to the XAI image editing API with the provided
   * parameters and returns the edited image.
   * <p>
   * This method is not thread-safe and should not be called concurrently on the
   * same instance.
   *
   * @param request the request object containing editing parameters such as the
   *                image to edit, mask, and prompt
   * @return a response object containing the edited image
   * @throws RuntimeException if the API request fails or the response is
   *                          invalid
   */
  public GeneratedImageResponse editImage(EditImageRequest request) {
    HttpRequest httpRequest = doPostJson(EDIT_IMAGES_PATH, request);
    return sendRequest(httpRequest, GeneratedImageResponse.class);
  }

}
