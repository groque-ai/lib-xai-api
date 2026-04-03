package com.xai.api.images.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ImageAspectRatio {

  @JsonProperty("1:1")
  ONE_TO_ONE("1:1"),
  @JsonProperty("3:4")
  THREE_TO_FOUR("3:4"),
  @JsonProperty("4:3")
  FOUR_TO_THREE("4:3"),
  @JsonProperty("9:16")
  NINE_TO_SIXTEEN("9:16"),
  @JsonProperty("16:9")
  SIXTEEN_TO_NINE("16:9"),
  @JsonProperty("2:3")
  TWO_TO_THREE("2:3"),
  @JsonProperty("3:2")
  THREE_TO_TWO("3:2"),
  @JsonProperty("9:19.5")
  NINE_TO_NINETEEN_POINT_FIVE("9:19.5"),
  @JsonProperty("19.5:9")
  NINETEEN_POINT_FIVE_TO_NINE("19.5:9"),
  @JsonProperty("9:20")
  NINE_TO_TWENTY("9:20"),
  @JsonProperty("20:9")
  TWENTY_TO_NINE("20:9"),
  @JsonProperty("1:2")
  ONE_TO_TWO("1:2"),
  @JsonProperty("2:1")
  TWO_TO_ONE("2:1");

  private final String value;

  ImageAspectRatio(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
