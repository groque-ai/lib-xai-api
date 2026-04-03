package com.xai.api.responses.config.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
  @JsonSubTypes.Type(value = SearchSourceX.class, name = "x"),
  @JsonSubTypes.Type(value = SearchSourceWeb.class, name = "web"),
  @JsonSubTypes.Type(value = SearchSourceNews.class, name = "news"),
  @JsonSubTypes.Type(value = SearchSourceRss.class, name = "rss")})
public abstract class SearchSource {

  @JsonProperty("type")
  private String type;

  public SearchSource(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
