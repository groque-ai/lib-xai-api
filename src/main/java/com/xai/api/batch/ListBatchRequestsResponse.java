package com.xai.api.batch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response wrapper for listing batch requests.
 */
public class ListBatchRequestsResponse {

  /**
   * Array of batch request metadata.
   */
  @JsonProperty("batch_request_metadata")
  public BatchRequestMetadata[] batchRequestMetadata;

  /**
   * Pagination token for retrieving the next page.
   */
  @JsonProperty("pagination_token")
  public String paginationToken;

  public BatchRequestMetadata[] getBatchRequestMetadata() {
    return batchRequestMetadata;
  }

  public void setBatchRequestMetadata(BatchRequestMetadata[] batchRequestMetadata) {
    this.batchRequestMetadata = batchRequestMetadata;
  }

  public String getPaginationToken() {
    return paginationToken;
  }

  public void setPaginationToken(String paginationToken) {
    this.paginationToken = paginationToken;
  }

}
