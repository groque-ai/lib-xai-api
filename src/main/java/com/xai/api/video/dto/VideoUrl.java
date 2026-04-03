package com.xai.api.video.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VideoUrl {

  /**
   * URL of the video (public URL or base64-encoded data URL).
   */
  @JsonProperty("url")
  private String url;

  public VideoUrl() {
  }

  public VideoUrl(String url) {
    this.url = url;
  }

  /**
   * Returns URL of the video (public URL or base64-encoded data URL).
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets URL of the video (public URL or base64-encoded data URL).
   *
   * @param url the url
   */
  public void setUrl(String url) {
    this.url = url;
  }
}
