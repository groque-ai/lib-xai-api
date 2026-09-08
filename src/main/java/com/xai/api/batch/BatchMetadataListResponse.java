package com.xai.api.batch;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response wrapper for listing batch requests.
 */
public class BatchMetadataListResponse {

  /**
   * Array of batch request metadata.
   */
  @JsonProperty("batch_request_metadata")
  public List<BatchMetadata> batchRequestMetadata;

  /**
   * Pagination token for retrieving the next page.
   */
  @JsonProperty("pagination_token")
  public String paginationToken;

  public List<BatchMetadata> getBatchRequestMetadata() {
    return batchRequestMetadata;
  }

  public void setBatchRequestMetadata(List<BatchMetadata> batchRequestMetadata) {
    this.batchRequestMetadata = batchRequestMetadata;
  }

  public String getPaginationToken() {
    return paginationToken;
  }

  public void setPaginationToken(String paginationToken) {
    this.paginationToken = paginationToken;
  }

}
