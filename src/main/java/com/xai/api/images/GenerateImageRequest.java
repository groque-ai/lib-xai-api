package com.xai.api.images;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.images.dto.ImageAspectRatio;
import com.xai.api.images.dto.ImageQuality;
import com.xai.api.images.dto.ImageResolution;
import com.xai.api.images.dto.ImageResponseFormat;

public class GenerateImageRequest {

  /**
   * Model to be used.
   */
  @JsonProperty("model")
  private String model;

  /**
   * Prompt for image generation.
   */
  @JsonProperty("prompt")
  private String prompt;

  /**
   * "Number of images to be generated" default	1 maximum	10 minimum	1
   */
  @JsonProperty("n")
  private int n = 1;

  /**
   * Response format to return the image in. Can be url or b64_json. If b64_json
   * is specified, the image will be returned as a base64-encoded string instead
   * of a url to the generated image file.
   */
  @JsonProperty("response_format")
  private ImageResponseFormat responseFormat = ImageResponseFormat.url;

  /**
   * Quality of the output image. Can be `low`, `medium`, or `high`. Currently a
   * no-op, reserved for future use.
   */
  @JsonProperty("quality")
  private ImageQuality quality;

  /**
   * Resolution of the generated image. Defaults to `1k`. Only supported by
   * grok-imagine models. Currently, only `1k` is supported. Support for `2k`
   * will be available shortly.
   */
  @JsonProperty("resolution")
  private ImageResolution resolution;

  /**
   * Aspect ratio of the generated image. Can be `1:1`, `3:4`, `4:3`, `9:16`,
   * `16:9`, `2:3`, `3:2`, `9:19.5`, `19.5:9`, `9:20`, `20:9`, `1:2`, `2:1`, or
   * `auto`. Defaults to `auto` for automatically selecting the best ratio for
   * the prompt. Only supported by grok-imagine models.
   */
  @JsonProperty("aspect_ratio")
  private ImageAspectRatio aspectRatio;

  /**
   * A unique identifier representing your end-user, which can help xAI to
   * monitor and detect abuse.
   */
  @JsonProperty("user")
  private String user;

  //  @Deprecated  @JsonProperty("size")  private String size;
  //  @Deprecated  @JsonProperty("style")  private String style;
  public ImageAspectRatio getAspectRatio() {
    return aspectRatio;
  }

  public void setAspectRatio(ImageAspectRatio aspectRatio) {
    this.aspectRatio = aspectRatio;
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
   * Returns Number of images to be generated
   *
   * @return the n
   */
  public int getN() {
    return n;
  }

  /**
   * Sets Number of images to be generated
   *
   * @param n the n
   */
  public void setN(int n) {
    this.n = n;
  }

  /**
   * Returns Prompt for image generation.
   *
   * @return the prompt
   */
  public String getPrompt() {
    return prompt;
  }

  /**
   * Sets Prompt for image generation.
   *
   * @param prompt the prompt
   */
  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  /**
   * Returns Quality of the output image. Can be `low`, `medium`, or `high`.
   * Currently a no-op, reserved for future use.
   *
   * @return the quality
   */
  public ImageQuality getQuality() {
    return quality;
  }

  /**
   * Sets Quality of the output image. Can be `low`, `medium`, or `high`.
   * Currently a no-op, reserved for future use.
   *
   * @param quality the quality
   */
  public void setQuality(ImageQuality quality) {
    this.quality = quality;
  }

  /**
   * Returns Resolution of the generated image. Defaults to `1k`. Only supported
   * by grok-imagine models. Currently, only `1k` is supported. Support for `2k`
   * will be available shortly.
   *
   * @return the resolution
   */
  public ImageResolution getResolution() {
    return resolution;
  }

  /**
   * Sets Resolution of the generated image. Defaults to `1k`. Only supported by
   * grok-imagine models. Currently, only `1k` is supported. Support for `2k`
   * will be available shortly.
   *
   * @param resolution the resolution
   */
  public void setResolution(ImageResolution resolution) {
    this.resolution = resolution;
  }

  public ImageResponseFormat getResponseFormat() {
    return responseFormat;
  }

  public void setResponseFormat(ImageResponseFormat responseFormat) {
    this.responseFormat = responseFormat;
  }

  /**
   * Returns A unique identifier representing your end-user, which can help xAI
   * to monitor and detect abuse.
   *
   * @return the user
   */
  public String getUser() {
    return user;
  }

  /**
   * Sets A unique identifier representing your end-user, which can help xAI to
   * monitor and detect abuse.
   *
   * @param user the user
   */
  public void setUser(String user) {
    this.user = user;
  }
}
