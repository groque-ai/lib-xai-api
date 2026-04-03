package com.xai.api.images.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImageUrl {

  /**
   * URL of the image.
   */
  @JsonProperty("url")
  private String url;

  public ImageUrl() {
  }

  public ImageUrl(String url) {
    this.url = url;
  }

  /**
   * Returns URL of the image.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets URL of the image.
   *
   * @param url the url
   */
  public void setUrl(String url) {
    this.url = url;
  }
}
