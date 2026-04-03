package com.xai.api.video.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum VideoResolution {

  @JsonProperty("480p")
  FOUR_EIGHTY_P("480p"),
  @JsonProperty("720p")
  SEVEN_TWENTY_P("720p");

  private final String value;

  VideoResolution(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
