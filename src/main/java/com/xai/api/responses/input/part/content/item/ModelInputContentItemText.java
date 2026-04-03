package com.xai.api.responses.input.part.content.item;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelInputContentItemText extends ModelInputContentItem {

  @JsonProperty("text")
  private String text;

  public ModelInputContentItemText() {
    super("text");
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

}
