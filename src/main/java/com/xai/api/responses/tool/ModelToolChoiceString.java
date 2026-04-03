package com.xai.api.responses.tool;

import com.fasterxml.jackson.annotation.JsonValue;
import com.xai.api.type.ToolChoice;

public class ModelToolChoiceString extends ModelToolChoice {

  /**
   * 'Controls tool access by the model. `"none"` makes model ignore tools,
   * `"auto"` let the model automatically decide whether to call a tool,
   * `"required"` forces model to pick a tool to call.'
   */
  @JsonValue
  private String value;

  public ModelToolChoiceString(String value) {
    this.value = value;
  }

  public ModelToolChoiceString() {
  }

  public ToolChoice getToolChoice() {
    return value == null ? null : ToolChoice.valueOf(value);
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value;
  }

}
