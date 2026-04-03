package com.xai.api.responses.input.part.content.item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ModelInputContentItemText.class, name = "text"),
  @JsonSubTypes.Type(value = ModelInputContentItemImage.class, name = "image"),
  @JsonSubTypes.Type(value = ModelInputContentItemFile.class, name = "file")})
public abstract class ModelInputContentItem {

  @JsonProperty("type")
  protected String type;

  public ModelInputContentItem(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
