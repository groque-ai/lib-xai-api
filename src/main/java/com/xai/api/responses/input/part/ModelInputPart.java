package com.xai.api.responses.input.part;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.xai.api.responses.input.part.item.ModelInputItem;
import com.xai.api.responses.input.part.item.ModelInputItemPayload;
import com.xai.api.type.Role;

/**
 * ModelInputPart[] = timeline of events the model should “see”
 * <p>
 * Messages = user/system instructions or conversation content
 * <p>
 * InputItems = previous outputs or local tool results the model should continue
 * reasoning from
 *
 * @author Key Bridge
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ // send new messages to the llm

  @JsonSubTypes.Type(ModelInputPartMessage.class), // send tool responses or regurgitate back to the llm
  @JsonSubTypes.Type(ModelInputItem.class)})
public abstract class ModelInputPart {

  public ModelInputPartMessage getInstanceMessage(Role role, String content) {
    return new ModelInputPartMessage(role, content);
  }

  public ModelInputItem getInstanceItem(ModelInputItemPayload payload) {
    return new ModelInputItem(payload);
  }
}
