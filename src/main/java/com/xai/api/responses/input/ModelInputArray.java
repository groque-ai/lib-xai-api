package com.xai.api.responses.input;

import com.fasterxml.jackson.annotation.JsonValue;
import com.xai.api.responses.input.part.ModelInputPart;
import java.util.ArrayList;
import java.util.List;

public class ModelInputArray extends ModelInput {

  @JsonValue
  private List<ModelInputPart> value;

  public List<ModelInputPart> getValue() {
    if (value == null) {
      value = new ArrayList<>();
    }
    return value;
  }

  public void setValue(List<ModelInputPart> value) {
    this.value = value;
  }

  public ModelInputArray addValue(ModelInputPart value) {
    getValue().add(value);
    return this;
  }
}
