package com.xai.api.images;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.images.dto.ImageQuality;
import com.xai.api.images.dto.ImageResolution;
import com.xai.api.images.dto.ImageUrl;

public class EditImageRequest {

  /**
   * Input image to perform edit on. Mutually exclusive with `images`.
   */
  @JsonProperty("image")
  private ImageUrl image;

  @JsonProperty("mask")
  private ImageUrl mask;

  /**
   * Model to be used.
   */
  @JsonProperty("model")
  private String model;

  /**
   * Number of image edits to be generated.
   */
  @JsonProperty("n")
  private int n = 1;

  /**
   * Prompt for image editing.
   */
  @JsonProperty("prompt")
  private String prompt;

  /**
   * Quality of the output image. Can be `low`, `medium`, or `high`. Currently a
   * no-op, reserved for future use.
   */
  @JsonProperty("quality")
  private ImageQuality //  Currently a no-op, reserved for future use." }
    quality;

  /**
   * Resolution of the generated image. Defaults to `1k`. Aspect ratio is
   * automatically detected from the input image. Only supported by grok-imagine
   * models.
   */
  @JsonProperty("resolution")
  private ImageResolution resolution;

  /**
   * Response format to return the image in. Can be `url` or `b64_json`. If
   * `b64_json` is specified, the image will be returned as a base64-encoded
   * string instead of a url to the generated image file.
   */
  @JsonProperty("response_format")
  private String responseFormat;

  /**
   * A unique identifier representing your end-user, which can help xAI to
   * monitor and detect abuse.
   */
  @JsonProperty("user")
  private String user;

  //  @JsonProperty("size")  private String size; // "(Not supported) Size of the image."
  //  @JsonProperty("style")  private String style;	// "(Not supported) Style of the image."
  /**
   * Returns Input image to perform edit on. Mutually exclusive with `images`.
   *
   * @return the image
   */
  public ImageUrl getImage() {
    return image;
  }

  /**
   * Sets Input image to perform edit on. Mutually exclusive with `images`.
   *
   * @param image the image
   */
  public void setImage(ImageUrl image) {
    this.image = image;
  }

  public void setImageUrl(String url) {
    this.image = new ImageUrl(url);
  }

  public ImageUrl getMask() {
    return mask;
  }

  public void setMask(ImageUrl mask) {
    this.mask = mask;
  }

  public void setMaskUrl(String url) {
    this.mask = new ImageUrl(url);
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
   * Returns Number of image edits to be generated.
   *
   * @return the n
   */
  public int getN() {
    return n;
  }

  /**
   * Sets Number of image edits to be generated.
   *
   * @param n the n
   */
  public void setN(int n) {
    this.n = n;
  }

  /**
   * Returns Prompt for image editing.
   *
   * @return the prompt
   */
  public String getPrompt() {
    return prompt;
  }

  /**
   * Sets Prompt for image editing.
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
   * Returns Resolution of the generated image. Defaults to `1k`. Aspect ratio
   * is automatically detected from the input image. Only supported by
   * grok-imagine models.
   *
   * @return the resolution
   */
  public ImageResolution getResolution() {
    return resolution;
  }

  /**
   * Sets Resolution of the generated image. Defaults to `1k`. Aspect ratio is
   * automatically detected from the input image. Only supported by grok-imagine
   * models.
   *
   * @param resolution the resolution
   */
  public void setResolution(ImageResolution resolution) {
    this.resolution = resolution;
  }

  public String getResponseFormat() {
    return responseFormat;
  }

  public void setResponseFormat(String responseFormat) {
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
