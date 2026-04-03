package com.xai.api.responses.config.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SearchSourceWeb extends SearchSource {

  @JsonProperty("allowed_websites")
  private List<String> allowedWebsites;

  @JsonProperty("excluded_websites")
  private List<String> excludedWebsites;

  @JsonProperty("country")
  private String country;

  @JsonProperty("safe_search")
  private Boolean safeSearch;

  public SearchSourceWeb() {
    super("web");
  }

  public List<String> getAllowedWebsites() {
    return allowedWebsites;
  }

  public void setAllowedWebsites(List<String> allowedWebsites) {
    this.allowedWebsites = allowedWebsites;
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
