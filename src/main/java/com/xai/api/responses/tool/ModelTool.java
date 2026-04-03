package com.xai.api.responses.tool;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.xai.api.type.ModelToolType;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = FunctionTool.class, name = "function"),
  @JsonSubTypes.Type(value = WebSearchTool.class, name = "web_search"),
  @JsonSubTypes.Type(value = XSearchTool.class, name = "x_search"),
  @JsonSubTypes.Type(value = KnowledgeBaseSearchTool.class, name = "knowledge_search"),
  @JsonSubTypes.Type(value = CodeInterpreterTool.class, name = "code_interpreter"),
  @JsonSubTypes.Type(value = McpServerTool.class, name = "mcp_server")})
public abstract class ModelTool {

  // All tool variants include a "type" discriminator
  protected ModelToolType type;

  public ModelTool(ModelToolType type) {
    this.type = type;
  }

  public ModelToolType getType() {
    return type;
  }

  public void setType(ModelToolType type) {
    this.type = type;
  }
}
