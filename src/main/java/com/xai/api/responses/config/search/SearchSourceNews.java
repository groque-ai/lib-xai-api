package com.xai.api.responses.config.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SearchSourceNews extends SearchSource {

  @JsonProperty("country")
  private String country;

  @JsonProperty("excluded_websites")
  private List<String> excludedWebsites;

  @JsonProperty("safe_search")
  private Boolean safeSearch;

  public SearchSourceNews() {
    super("news");
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public List<String> getExcludedWebsites() {
    return excludedWebsites;
  }

  public void setExcludedWebsites(List<String> excludedWebsites) {
    this.excludedWebsites = excludedWebsites;
  }

  public Boolean getSafeSearch() {
    return safeSearch;
  }

  public void setSafeSearch(Boolean safeSearch) {
    this.safeSearch = safeSearch;
  }

}
