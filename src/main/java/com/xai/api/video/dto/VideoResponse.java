package com.xai.api.video.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The xAI API polling endpoint for deferred video generation returns different
 * top-level JSON objects based on status:
 * <p>
 * Pending (processing): { "status": "pending" } (or with additional fields in
 * some cases)
 * <p>
 * Done (complete): { "video": { ... }, "model": "grok-imagine-video" } (no
 * "status" field present)
 * <p>
 * Expired / error — likely includes "status": "expired" or error details
 * <p>
 * This is a discriminated union style response (common in async APIs), not a
 * single object with a conditional field.
 *
 * @author Key Bridge
 */
public class VideoResponse {

  // Status of the deferred request: "pending" or "done".
  private VideoStatus status;

  /**
   * The model used to generate the video.
   */
  @JsonProperty("model")
  private String model;

  /**
   * The generated video data.
   */
  @JsonProperty("video")
  private GeneratedVideo video;

  /**
   * Returns The model used to generate the video.
   *
   * @return the model
   */
  public String getModel() {
    return model;
  }

  /**
   * Sets The model used to generate the video.
   *
   * @param model the model
   */
  public void setModel(String model) {
    this.model = model;
  }

  /**
   * Returns The generated video data.
   *
   * @return the video
   */
  public GeneratedVideo getVideo() {
    return video;
  }

  /**
   * Sets The generated video data.
   *
   * @param video the video
   */
  public void setVideo(GeneratedVideo video) {
    this.video = video;
  }

  public VideoStatus getStatus() {
    return status;
  }

  public void setStatus(VideoStatus status) {
    this.status = status;
  }

  @JsonIgnore
  public boolean isPending() {
    return VideoStatus.pending.equals(status);
  }

  @JsonIgnore
  public boolean isDone() {
    return video != null && video.getUrl() != null;
  }
}
