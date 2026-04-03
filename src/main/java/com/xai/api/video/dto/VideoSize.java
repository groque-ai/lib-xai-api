package com.xai.api.video.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

@Deprecated
public enum VideoSize {

  @JsonProperty("848x480")
  SIZE_848x480("848x480"),
  @JsonProperty("1696x960")
  SIZE_1696x960("1696x960"),
  @JsonProperty("1280x720")
  SIZE_1280x720("1280x720"),
  @JsonProperty("1920x1080")
  SIZE_1920x1080("1920x1080");

  private final String value;

  VideoSize(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
