package com.xai.api.batch;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 *
 * @author Key Bridge
 */
public class BatchResultsResponse {

  /**
   * The results that has been processed.
   */
  @JsonProperty("results")
  public List<BatchResult> results;

  /**
   * The page token to retrieve batches from the next page. Will be empty if
   * this is the last page.
   */
  @JsonProperty("pagination_token")
  public String paginationToken;

  public String getPaginationToken() {
    return paginationToken;
  }

  public void setPaginationToken(String paginationToken) {
    this.paginationToken = paginationToken;
  }

  public List<BatchResult> getResults() {
    return results;
  }

  public void setResults(List<BatchResult> results) {
    this.results = results;
  }
}
