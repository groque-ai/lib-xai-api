package com.xai.api.batch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 *
 * @author Key Bridge
 */
public class BatchResult {

  /**
   * Unique identifier of the request within the batch.
   */
  @JsonProperty("batch_request_id")
  public String batchRequestId;

  /**
   * The batch result.
   */
  @JsonProperty("batch_result")
  public JsonNode batchResult;

  public String getBatchRequestId() {
    return batchRequestId;
  }

  public void setBatchRequestId(String batchRequestId) {
    this.batchRequestId = batchRequestId;
  }

  public JsonNode getBatchResult() {
    return batchResult;
  }

  public void setBatchResult(JsonNode batchResult) {
    this.batchResult = batchResult;
  }

}
