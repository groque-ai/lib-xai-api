package com.xai.api.batches;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response wrapper for listing batches (GET /v1/batches).
 */
public class ListBatchesResponse {

  /**
   * Array of batch objects.
   */
  @JsonProperty("batches")
  public List<Batch> batches;

  /**
   * Pagination token for retrieving the next page.
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
