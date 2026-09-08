package com.xai.api.batch;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.images.EditImageRequest;
import com.xai.api.images.GenerateImageRequest;
import com.xai.api.responses.ModelRequest;
import com.xai.api.video.EditVideoRequest;
import com.xai.api.video.GenerateVideoRequest;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Represents a batch request container that can hold one of several supported
 * request types for unified processing.
 */
@JsonInclude(NON_NULL)
public class BatchRequest {

  @JsonProperty("responses")
  private ModelRequest responses;

  @JsonProperty("image_generation")
  private GenerateImageRequest imageGeneration;

  @JsonProperty("image_edit")
  private EditImageRequest imageEdit;

  @JsonProperty("video_generation")
  private GenerateVideoRequest videoGeneration;

  @JsonProperty("video_extension")
  private EditVideoRequest videoExtension;

  public BatchRequest() {
    // No-op: fields are initialized to null by default
  }

  /**
   * Static factory method that creates a new BatchRequest instance and
   * populates the appropriate field based on the runtime type of the provided
   * object.
   * <p>
   * <p>
   * Only one field will be set per invocation. If the object does not match any
   * known request type, the returned instance will have all fields as null.
   *
   * @param request the request object to be assigned (may be null)
   * @return a new BatchRequest with the matching field populated
   */
  public static BatchRequest getInstance(Object request) {
    BatchRequest batchRequest = new BatchRequest();

    if (request == null) {
      // Developer note: Returning an empty instance for null input
      // to maintain consistent contract and avoid NullPointerException
      // at call sites. Callers should check for populated state if needed.
      throw new IllegalArgumentException("Request cannot be null");
    }

    // Developer note: Using explicit if-else instanceof chain instead of
    // a switch or map-based dispatch. This approach is JDK 11 compatible
    // and provides clear, predictable type resolution without reflection.
    // Only the first matching type is assigned (no multiple assignments).
    if (request instanceof ModelRequest) {
      batchRequest.responses = (ModelRequest) request;
    } else if (request instanceof GenerateImageRequest) {
      batchRequest.imageGeneration = (GenerateImageRequest) request;
    } else if (request instanceof EditImageRequest) {
      batchRequest.imageEdit = (EditImageRequest) request;
    } else if (request instanceof GenerateVideoRequest) {
      batchRequest.videoGeneration = (GenerateVideoRequest) request;
    } else if (request instanceof EditVideoRequest) {
      batchRequest.videoExtension = (EditVideoRequest) request;
    }
    // Developer note: Unknown types are intentionally ignored.
    // This keeps the method lenient and prevents failure on unexpected
    // input while still supporting the defined request types.

    return batchRequest;
  }

  public EditImageRequest getImageEdit() {
    return imageEdit;
  }

  public void setImageEdit(EditImageRequest imageEdit) {
    this.imageEdit = imageEdit;
  }

  public GenerateImageRequest getImageGeneration() {
    return imageGeneration;
  }

  public void setImageGeneration(GenerateImageRequest imageGeneration) {
    this.imageGeneration = imageGeneration;
  }

  public ModelRequest getResponses() {
    return responses;
  }

  public void setResponses(ModelRequest responses) {
    this.responses = responses;
  }

  public EditVideoRequest getVideoExtension() {
    return videoExtension;
  }

  public void setVideoExtension(EditVideoRequest videoExtension) {
    this.videoExtension = videoExtension;
  }

  public GenerateVideoRequest getVideoGeneration() {
    return videoGeneration;
  }

  public void setVideoGeneration(GenerateVideoRequest videoGeneration) {
    this.videoGeneration = videoGeneration;
  }

}
