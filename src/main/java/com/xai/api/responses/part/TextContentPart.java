package com.xai.api.responses.part;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("text")
public class TextContentPart extends ContentPart {

  @JsonProperty("text")
  private String text;

  public TextContentPart() {
    super("text");
  }

  public TextContentPart(String text) {
    this();
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

}
