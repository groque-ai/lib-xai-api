package com.xai.api.video.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum VideoAspectRatio {

  @JsonProperty("1:1")
  ONE_TO_ONE("1:1"),
  @JsonProperty("16:9")
  SIXTEEN_TO_NINE("16:9"),
  @JsonProperty("9:16")
  NINE_TO_SIXTEEN("9:16"),
  @JsonProperty("4:3")
  FOUR_TO_THREE("4:3"),
  @JsonProperty("3:4")
  THREE_TO_FOUR("3:4"),
  @JsonProperty("3:2")
  THREE_TO_TWO("3:2"),
  @JsonProperty("2:3")
  TWO_TO_THREE("2:3");

  private final String value;

  VideoAspectRatio(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
