package com.xai.api.responses.config.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SearchSourceRss extends SearchSource {

  @JsonProperty("links")
  private List<String> links;

  public SearchSourceRss() {
    super("rss");
  }

  public List<String> getLinks() {
    return links;
  }

  public void setLinks(List<String> links) {
    this.links = links;
  }

}
