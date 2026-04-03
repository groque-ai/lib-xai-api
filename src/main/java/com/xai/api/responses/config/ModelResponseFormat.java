package com.xai.api.responses.config;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ModelResponseFormatText.class, name = "text"),
  @JsonSubTypes.Type(value = ModelResponseFormatJsonObject.class, name = "json_object"),
  @JsonSubTypes.Type(value = ModelResponseFormatJsonSchema.class, name = "json_schema")})
public abstract class ModelResponseFormat {

  // Discriminator: "text", "json_object", or "json_schema"
  protected String type;

  public ModelResponseFormat(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
