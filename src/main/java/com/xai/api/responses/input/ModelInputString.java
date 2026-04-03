package com.xai.api.responses.input;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Key Bridge
 * @deprecated convenience / legacy / shorthand. this is a shortcut. use
 * ModelInputArray of ModelInputPartMessage to specify the role and content.
 */
@Deprecated
public class ModelInputString extends ModelInput {

  @JsonValue
  private String value;

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
