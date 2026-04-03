package com.xai.api.responses.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.type.ModelToolType;

public class CodeInterpreterTool extends ModelTool {

  @JsonProperty("container")
  private Object container;

  public CodeInterpreterTool() {
    super(ModelToolType.code_interpreter);
  }

  public Object getContainer() {
    return container;
  }

  public void setContainer(Object container) {
    this.container = container;
  }

}
