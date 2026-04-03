package com.xai.api.video;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.video.dto.VideoOutput;
import com.xai.api.video.dto.VideoUrl;

public class EditVideoRequest {

  /**
   * Model to be used.
   */
  @JsonProperty("model")
  private String model;

  /**
   * Optional output destination for generated video.
   */
  @JsonProperty("output")
  private VideoOutput output;

  /**
   * Prompt for video editing.
   */
  @JsonProperty("prompt")
  private String prompt;

  /**
   * A unique identifier representing your end-user.
   */
  @JsonProperty("user")
  private String user;

  /**
   * Input video to perform edit on.
   */
  @JsonProperty("video")
  private VideoUrl video;

  /**
   * Returns Model to be used.
   *
   * @return the model
   */
  public String getModel() {
    return model;
  }

  /**
   * Sets Model to be used.
   *
   * @param model the model
   */
  public void setModel(String model) {
    this.model = model;
  }

  /**
   * Returns Optional output destination for generated video.
   *
   * @return the output
   */
  public VideoOutput getOutput() {
    return output;
  }

  /**
   * Sets Optional output destination for generated video.
   *
   * @param output the output
   */
  public void setOutput(VideoOutput output) {
    this.output = output;
  }

  /**
   * Returns Prompt for video editing.
   *
   * @return the prompt
   */
  public String getPrompt() {
    return prompt;
  }

  /**
   * Sets Prompt for video editing.
   *
   * @param prompt the prompt
   */
  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  /**
   * Returns A unique identifier representing your end-user.
   *
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * Sets A unique identifier representing your end-user.
   *
   * @param user the user
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * Returns Input video to perform edit on.
   *
   * @return the video
   */
  public VideoUrl getVideo() {
    return video;
  }

  /**
   * Sets Input video to perform edit on.
   *
   * @param video the video
   */
  public void setVideo(VideoUrl video) {
    this.video = video;
  }
}
