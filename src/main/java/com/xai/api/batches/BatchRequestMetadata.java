package com.xai.api.batches;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BatchRequestMetadata {

  @JsonProperty("batch_request_id")
  private String batchRequestId;
  @JsonProperty("endpoint")
  private String endpoint;
  @JsonProperty("model")
  private String model;

  @JsonProperty("state")
  private String state;

  @JsonProperty("create_time")
  private String createTime;
  @JsonProperty("finish_time")
  private String finishTime;

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
