package com.xai.api.images.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ImageQuality {

  @JsonProperty("low")
  LOW("low"),
  @JsonProperty("medium")
  MEDIUM("medium"),
  @JsonProperty("high")
  HIGH("high");

  private final String value;

  ImageQuality(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
