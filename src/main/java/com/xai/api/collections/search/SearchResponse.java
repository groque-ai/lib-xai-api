package com.xai.api.collections.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SearchResponse {

  /**
   * The search matches.
   */
  @JsonProperty("matches")
  private List<SearchMatch> matches;

  /**
   * Returns The search matches.
   *
   * @return the matches
   */
  public List<SearchMatch> getMatches() {
    return matches;
  }

  /**
   * Sets The search matches.
   *
   * @param matches the matches
   */
  public void setMatches(List<SearchMatch> matches) {
    this.matches = matches;
  }
}
