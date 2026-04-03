package com.xai.api.responses.input.part.content;

import com.fasterxml.jackson.annotation.JsonValue;

public class ModelInputContentString extends ModelInputContent {

  @JsonValue
  private String value;

  public ModelInputContentString() {
  }

  public ModelInputContentString(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
