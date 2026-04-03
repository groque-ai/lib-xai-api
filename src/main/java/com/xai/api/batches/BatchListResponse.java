package com.xai.api.batches;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * List batch requests in a batch
 * <p>
 * GET /v1/batches/{batch_id}/requests JSON
 * <p>
 * List metadata for all requests in a batch.
 *
 * @author Key Bridge
 */
public class BatchListResponse {

  @JsonProperty("batch_request_metadata")
  private List<BatchRequestMetadata> batchRequestMetadata;

  @JsonProperty("pagination_token")
  private String paginationToken; // The page token to retrieve results from the next page. Will be empty if this is the last page.

  public List<BatchRequestMetadata> getBatchRequestMetadata() {
    return batchRequestMetadata;
  }

  public void setBatchRequestMetadata(List<BatchRequestMetadata> batchRequestMetadata) {
    this.batchRequestMetadata = batchRequestMetadata;
  }

  public String getPaginationToken() {
    return paginationToken;
  }

  public void setPaginationToken(String paginationToken) {
    this.paginationToken = paginationToken;
  }
}
