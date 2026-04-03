package com.xai.api.responses.tool;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.type.ModelToolType;
import java.util.List;

public class WebSearchTool extends ModelTool {

  @JsonProperty("allowed_domains")
  private List<String> allowedDomains;

  @JsonProperty("enable_image_understanding")
  private Boolean enableImageUnderstanding;

  @JsonProperty("excluded_domains")
  private List<String> excludedDomains;

  @JsonProperty("external_web_access")
  private Boolean externalWebAccess;

  @JsonProperty("filters")
  private Object filters;

  @JsonProperty("search_context_size")
  private String searchContextSize;

  @JsonProperty("user_location")
  private Object userLocation;

  public WebSearchTool() {
    super(ModelToolType.web_search);
  }

  public List<String> getAllowedDomains() {
    return allowedDomains;
  }

  public void setAllowedDomains(List<String> allowedDomains) {
    this.allowedDomains = allowedDomains;
  }

  public Boolean getEnableImageUnderstanding() {
    return enableImageUnderstanding;
  }

  public void setEnableImageUnderstanding(Boolean enableImageUnderstanding) {
    this.enableImageUnderstanding = enableImageUnderstanding;
  }

  public List<String> getExcludedDomains() {
    return excludedDomains;
  }

  public void setExcludedDomains(List<String> excludedDomains) {
    this.excludedDomains = excludedDomains;
  }

  public Boolean getExternalWebAccess() {
    return externalWebAccess;
  }

  public void setExternalWebAccess(Boolean externalWebAccess) {
    this.externalWebAccess = externalWebAccess;
  }

  public Object getFilters() {
    return filters;
  }

  public void setFilters(Object filters) {
    this.filters = filters;
  }

  public String getSearchContextSize() {
    return searchContextSize;
  }

  public void setSearchContextSize(String searchContextSize) {
    this.searchContextSize = searchContextSize;
  }

  public Object getUserLocation() {
    return userLocation;
  }

  public void setUserLocation(Object userLocation) {
    this.userLocation = userLocation;
  }
}
