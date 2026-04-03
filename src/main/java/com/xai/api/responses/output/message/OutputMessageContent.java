package com.xai.api.responses.output.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.xai.api.type.OutputMessageContentType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = OutputMessageContentText.class, name = "output_text"),
  @JsonSubTypes.Type(value = OutputMessageContentRefusal.class, name = "refusal")})
public abstract class OutputMessageContent {

  // Discriminator field ("output_text" or "refusal")
  protected OutputMessageContentType type;

  public OutputMessageContent(OutputMessageContentType type) {
    this.type = type;
  }

  public OutputMessageContentType getType() {
    return type;
  }
}
