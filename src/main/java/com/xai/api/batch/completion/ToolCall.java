package com.xai.api.batch.completion;



import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a tool call requested by the model.
 */
public class ToolCall {

  /**
   * Unique ID of the tool call.
   */
  @JsonProperty("id")
  private String id;

  /**
   * Function to call for the tool call.
   */
  @JsonProperty("function")
  private Function function;

  /**
   * Index of the tool call.
   */
  @JsonProperty("index")
  private Integer index;

  /**
   * Type of tool call (function, web_search_call, etc.).
   */
  @JsonProperty("type")
  private String type;

  public Function getFunction() {
    return function;
  }

  public void setFunction(Function function) {
    this.function = function;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Integer getIndex() {
    return index;
  }

  public void setIndex(Integer index) {
    this.index = index;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
