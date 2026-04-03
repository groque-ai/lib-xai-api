package com.xai.api.video;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.images.dto.ImageUrl;
import com.xai.api.video.dto.VideoAspectRatio;
import com.xai.api.video.dto.VideoOutput;
import com.xai.api.video.dto.VideoResolution;

public class GenerateVideoRequest {

  /**
   * Video duration in seconds. Range: [1, 15]. Default: 8.
   */
  @JsonProperty("duration")
  private int //	"Video duration in seconds. Range: [1, 15]. Default: 8."
    duration = 8;

  /**
   * Optional input image for image-to-video generation. If provided, generates
   * video with this image as the first frame.
   */
  @JsonProperty("image")
  private ImageUrl image;

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
   * Prompt for video generation.
   */
  @JsonProperty("prompt")
  private String prompt;

  /**
   * Aspect ratio of the generated video.
   */
  @JsonProperty("aspect_ratio")
  private VideoAspectRatio aspectRatio;

  /**
   * Resolution of the generated video.
   */
  @JsonProperty("resolution")
  private VideoResolution resolution;

  /**
   * A unique identifier representing your end-user.
   */
  @JsonProperty("user")
  private String user;

  //  @JsonProperty("size")  private VideoSize size;	// { "$ref": "#/components/schemas/VideoSize", description: 'The size of the generated video. DEPRECATED: Use aspect_ratio and resolution instead.\nDefaults to "848x480" (16:9) if not specified.' }
  public VideoAspectRatio getAspectRatio() {
    return aspectRatio;
  }

  public void setAspectRatio(VideoAspectRatio aspectRatio) {
    this.aspectRatio = aspectRatio;
  }

  /**
   * Returns Video duration in seconds. Range: [1, 15]. Default: 8.
   *
   * @return the duration
   */
  public int getDuration() {
    return duration;
  }

  /**
   * Sets Video duration in seconds. Range: [1, 15]. Default: 8.
   *
   * @param duration the duration
   */
  public void setDuration(int duration) {
    this.duration = duration;
  }

  /**
   * Returns Optional input image for image-to-video generation. If provided,
   * generates video with this image as the first frame.
   *
   * @return the image
   */
  public ImageUrl getImage() {
    return image;
  }

  /**
   * Sets Optional input image for image-to-video generation. If provided,
   * generates video with this image as the first frame.
   *
   * @param image the image
   */
  public void setImage(ImageUrl image) {
    this.image = image;
  }

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
   * Returns Prompt for video generation.
   *
   * @return the prompt
   */
  public String getPrompt() {
    return prompt;
  }

  /**
   * Sets Prompt for video generation.
   *
   * @param prompt the prompt
   */
  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  /**
   * Returns Resolution of the generated video.
   *
   * @return the resolution
   */
  public VideoResolution getResolution() {
    return resolution;
  }

  /**
   * Sets Resolution of the generated video.
   *
   * @param resolution the resolution
   */
  public void setResolution(VideoResolution resolution) {
    this.resolution = resolution;
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
}
