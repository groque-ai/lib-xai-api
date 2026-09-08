package com.xai.api.batch;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response wrapper for listing batches (GET /v1/batches).
 */
public class BatchListResponse {

  /**
   * Array of batch objects.
   */
  @JsonProperty("batches")
  public List<Batch> batches;

  /**
   * The page token to retrieve batches from the next page. Will be empty if
   * this is the last page.
   */
  @JsonProperty("pagination_token")
  public String paginationToken;

  public List<Batch> getBatches() {
    return batches;
  }

  public void setBatches(List<Batch> batches) {
    this.batches = batches;
  }

  public String getPaginationToken() {
    return paginationToken;
  }

  public void setPaginationToken(String paginationToken) {
    this.paginationToken = paginationToken;
  }
}
