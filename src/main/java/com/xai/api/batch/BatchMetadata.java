package com.xai.api.batch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Metadata for a single request within a batch (GET
 * /v1/batches/{batch_id}/requests).
 */
public class BatchMetadata {

  /**
   * Unique identifier of the request within the batch.
   */
  @JsonProperty("batch_request_id")
  public String batchRequestId;

  /**
   * API endpoint queried.
   */
  @JsonProperty("endpoint")
  public String endpoint;

  /**
   * Model name used for the request.
   */
  @JsonProperty("model")
  public String model;

  /**
   * Time when the request was created.
   */
  @JsonProperty("create_time")
  public String createTime;

  /**
   * Time when the request finished.
   */
  @JsonProperty("finish_time")
  public String finishTime;

  /**
   * Current state of the request.
   * <p>
   * ["unknown" | "pending" | "succeeded" | "cancelled" | "failed"]
   */
  @JsonProperty("state")
  public String state;

  public String getBatchRequestId() {
    return batchRequestId;
  }

  public void setBatchRequestId(String batchRequestId) {
    this.batchRequestId = batchRequestId;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getFinishTime() {
    return finishTime;
  }

  public void setFinishTime(String finishTime) {
    this.finishTime = finishTime;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }
}
