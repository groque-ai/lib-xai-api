package com.xai.api.batch.completion;



import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a function definition for a tool call.
 */
public class Function {

  /**
   * Name of the function.
   */
  @JsonProperty("name")
  private String name;

  /**
   * Arguments for the function call.
   */
  @JsonProperty("arguments")
  private String arguments;

  public String getArguments() {
    return arguments;
  }

  public void setArguments(String arguments) {
    this.arguments = arguments;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
