package com.xai.api.batch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the result of a single request within a batch. Returned by GET
 * /v1/batches/{batch_id}/results.
 */
public class BatchAddResponse {

  /**
   * Identifier of the request within the batch.
   */
  @JsonProperty("batch_request_id")
  public String batchRequestId;

  /**
   * Response payload, varies by request type (chat, image, video).
   */
  @JsonProperty("response")
  public Object response;

  /**
   * Error message if the request failed.
   */
  @JsonProperty("error")
  public String error;

  public String getBatchRequestId() {
    return batchRequestId;
  }

  public void setBatchRequestId(String batchRequestId) {
    this.batchRequestId = batchRequestId;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public Object getResponse() {
    return response;
  }

  public void setResponse(Object response) {
    this.response = response;
  }
}
