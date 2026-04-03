package com.xai.api.responses.config;

import com.fasterxml.jackson.databind.JsonNode;

public class ModelResponseFormatJsonSchema extends ModelResponseFormat {

  // Only included for compatibility
  private String description;
  // Only included for compatibility
  private String name;
  // Only included for compatibility
  private Boolean strict;

  // A JSON schema representing the desired response schema
  private JsonNode schema;

  public ModelResponseFormatJsonSchema() {
    super("json_schema");
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public JsonNode getSchema() {
    return schema;
  }

  public void setSchema(JsonNode schema) {
    this.schema = schema;
  }

  public Boolean getStrict() {
    return strict;
  }

  public void setStrict(Boolean strict) {
    this.strict = strict;
  }

}
