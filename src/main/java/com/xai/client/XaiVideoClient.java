package com.xai.client;

import com.xai.api.video.EditVideoRequest;
import com.xai.api.video.GenerateVideoRequest;
import com.xai.api.video.StartDeferredResponse;
import com.xai.api.video.dto.VideoResponse;
import java.net.http.HttpRequest;

/**
 * Client for interacting with XAI video generation, editing, and retrieval
 * services.
 * <p>
 * This class provides methods to generate new videos, edit existing videos, and
 * retrieve generated videos by their request ID.
 * <p>
 * Usage notes: Instances of this class should be closed after use to release
 * underlying HTTP client resources.
 * <p>
 * Concurrency assumptions: This class is thread-safe for concurrent invocations
 * of its methods, as it inherits thread-safety from {@link XaiAbstractClient}.
 * <p>
 * Resource management: Implements {@link AutoCloseable} via inheritance; call
 * {@link #close()} to release resources.
 *
 * @author Key Bridge
 */
public class XaiVideoClient extends XaiAbstractClient {

  private static final String GENERATE_VIDEO_PATH = "/videos/generations";
  private static final String EDIT_VIDEO_PATH = "/videos/edits";
  private static final String GET_VIDEO_PATH = "/videos";

  /**
   * Constructs a new XaiVideoClient.
   * <p>
   * Initializes the client with default configuration loaded from external
   * sources.
   * <p>
   * Usage notes: Ensure to call {@link #close()} when done to release
   * resources.
   */
  public XaiVideoClient() {
    super(""); // paths are assigned per-method
  }

  /**
   * Initiates the generation of a new video based on the provided request.
   * <p>
   * Sends a POST request to the video generations endpoint and returns the
   * response indicating the start of a deferred operation.
   *
   * @param request the video generation request containing parameters such as
   *                prompt, model, etc.; must not be null
   * @return the response containing the request ID and status of the video
   *         generation operation
   * @throws ApiHttpException     if an HTTP error occurs during the request
   * @throws ApiParseException    if JSON serialization or deserialization fails
   * @throws NullPointerException if request is null
   */
  public StartDeferredResponse createVideo(GenerateVideoRequest request) {
    HttpRequest httpRequest = doPostJson(GENERATE_VIDEO_PATH, request);
    return sendRequest(httpRequest, StartDeferredResponse.class);
  }

  /**
   * Initiates the editing of an existing video based on the provided request.
   * <p>
   * Sends a POST request to the video edits endpoint and returns the response
   * indicating the start of a deferred operation.
   *
   * @param request the video edit request containing parameters such as video
   *                ID, edits, etc.; must not be null
   * @return the response containing the request ID and status of the video
   *         editing operation
   * @throws ApiHttpException     if an HTTP error occurs during the request
   * @throws ApiParseException    if JSON serialization or deserialization fails
   * @throws NullPointerException if request is null
   */
  public StartDeferredResponse editVideo(EditVideoRequest request) {
    HttpRequest httpRequest = doPostJson(EDIT_VIDEO_PATH, request);
    return sendRequest(httpRequest, StartDeferredResponse.class);
  }

  /**
   * Retrieves the status and details of a video generation or editing operation
   * by its request ID.
   * <p>
   * Sends a GET request to the videos endpoint with the specified request ID.
   *
   * @param requestId the unique identifier of the video request; must not be
   *                  null or empty
   * @return the video response containing the video data, status, or null if
   *         not found (404)
   * @throws ApiHttpException         if an HTTP error occurs during the request
   *                                  (except 404)
   * @throws ApiParseException        if JSON deserialization fails
   * @throws NullPointerException     if requestId is null
   * @throws IllegalArgumentException if requestId is empty
   */
  public VideoResponse getVideo(String requestId) {
    if (requestId == null) {
      throw new NullPointerException("requestId cannot be null");
    }
    if (requestId.trim().isEmpty()) {
      throw new IllegalArgumentException("requestId cannot be empty");
    }
    HttpRequest request = doGet(GET_VIDEO_PATH + "/" + requestId);
    return sendRequest(request, VideoResponse.class);
  }

}
