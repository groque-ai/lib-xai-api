package com.xai.api.images.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ImageResolution {

  @JsonProperty("1k")
  ONE_K("1k"),
  @JsonProperty("2k")
  TWO_K("2k");

  private final String value;

  ImageResolution(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
