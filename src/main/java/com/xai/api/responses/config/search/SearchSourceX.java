package com.xai.api.responses.config.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SearchSourceX extends SearchSource {

  @JsonProperty("excluded_x_handles")
  private List<String> excludedXHandles;

  @JsonProperty("included_x_handles")
  private List<String> includedXHandles;

  @JsonProperty("x_handles")
  private List<String> xHandles; // deprecated

  @JsonProperty("post_favorite_count")
  private Integer postFavoriteCount;

  @JsonProperty("post_view_count")
  private Integer postViewCount;

  public SearchSourceX() {
    super("x");
  }

  public List<String> getExcludedXHandles() {
    return excludedXHandles;
  }

  public void setExcludedXHandles(List<String> excludedXHandles) {
    this.excludedXHandles = excludedXHandles;
  }

  public List<String> getIncludedXHandles() {
    return includedXHandles;
  }

  public void setIncludedXHandles(List<String> includedXHandles) {
    this.includedXHandles = includedXHandles;
  }

  public Integer getPostFavoriteCount() {
    return postFavoriteCount;
  }

  public void setPostFavoriteCount(Integer postFavoriteCount) {
    this.postFavoriteCount = postFavoriteCount;
  }

  public Integer getPostViewCount() {
    return postViewCount;
  }

  public void setPostViewCount(Integer postViewCount) {
    this.postViewCount = postViewCount;
  }

  public List<String> getxHandles() {
    return xHandles;
  }

  public void setxHandles(List<String> xHandles) {
    this.xHandles = xHandles;
  }
}
