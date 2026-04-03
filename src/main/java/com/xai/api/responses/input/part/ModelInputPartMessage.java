package com.xai.api.responses.input.part;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.responses.input.part.content.ModelInputContent;
import com.xai.api.responses.input.part.content.ModelInputContentString;
import com.xai.api.type.Role;

public class ModelInputPartMessage extends ModelInputPart {

  @JsonProperty("type")
  private String type = "message";  // always "message"

  @JsonProperty("role")
  private Role role;

  @JsonProperty("content")
  private ModelInputContent content; // / String (text) OR List<ModelInputContentItem> (multimodal)

  @JsonProperty("name")
  private String name; // end-user

  public ModelInputPartMessage() {
  }

  public ModelInputPartMessage(Role role, String content) {
    this.role = role;
    this.content = new ModelInputContentString(content);
  }

  public ModelInputContent getContent() {
    return content;
  }

  public void setContent(ModelInputContent content) {
    this.content = content;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public String getType() {
    return type;
  }

  @Deprecated
  public void setType(String type) {
    this.type = type;
  }
}
