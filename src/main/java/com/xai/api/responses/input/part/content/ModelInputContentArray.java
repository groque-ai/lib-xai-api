package com.xai.api.responses.input.part.content;

import com.fasterxml.jackson.annotation.JsonValue;
import com.xai.api.responses.input.part.content.item.ModelInputContentItem;
import java.util.List;

@Deprecated // not supported by grok api
public class ModelInputContentArray extends ModelInputContent {

  @JsonValue
  private List<ModelInputContentItem> value;

  public List<ModelInputContentItem> getValue() {
    return value;
  }

  public void setValue(List<ModelInputContentItem> value) {
    this.value = value;
  }
}
