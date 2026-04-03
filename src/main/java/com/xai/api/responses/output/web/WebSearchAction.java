package com.xai.api.responses.output.web;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = WebSearchActionSearch.class, name = "search"),
  @JsonSubTypes.Type(value = WebSearchActionOpenPage.class, name = "open_page"),
  @JsonSubTypes.Type(value = WebSearchActionFind.class, name = "find")})
public abstract class WebSearchAction {

  // Discriminator: "search", "open_page", or "find"
  private String type;
}
