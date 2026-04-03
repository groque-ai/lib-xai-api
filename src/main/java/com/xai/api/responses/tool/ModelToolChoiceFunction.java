package com.xai.api.responses.tool;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModelToolChoiceFunction extends ModelToolChoice {

  @JsonProperty("type")
  private String type = "function";  // 'Type is always `"function"`.'

  @JsonProperty("name")
  private String name; // "Name of the function to use."

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return "ModelToolChoiceFunction{" + "type=" + type + ", name=" + name + '}';
  }

}
