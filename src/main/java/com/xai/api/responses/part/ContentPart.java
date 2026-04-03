package com.xai.api.responses.part;

// Base for content parts (used when content is an array)
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
  @JsonSubTypes.Type(value = TextContentPart.class, name = "text"),
  @JsonSubTypes.Type(value = ImageUrlContentPart.class, name = "image_url")})
public abstract class ContentPart {

  /**
   * The type of the content part.
   */
  @JsonProperty("type")
  protected String type;

  protected ContentPart(String type) {
    this.type = type;
  }

  /**
   * Returns The type of the content part.
   *
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets The type of the content part.
   *
   * @param type the type
   */
  public void setType(String type) {
    this.type = type;
  }
}
