package com.xai.api.batches;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a batch entity in the xAI Inference API. Returned by endpoints
 * such as POST /v1/batches, GET /v1/batches/{batch_id}, and cancel operations.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Batch {

  /**
   * Unique identifier of the batch.
   */
  @JsonProperty("batch_id")
  public String batchId;

  /**
   * Human-readable name of the batch.
   */
  @JsonProperty("name")
  public String name;

  /**
   * Time when the batch was created (ISO-8601).
   */
  @JsonProperty("create_time")
  public String createTime;

  /**
   * Time when the batch expires (ISO-8601).
   */
  @JsonProperty("expire_time")
  public String expireTime;

  /**
   * ID of the API key used to create the batch.
   */
  @JsonProperty("create_api_key_id")
  public String createApiKeyId;

  /**
   * Time when the batch was cancelled (ISO-8601).
   */
  @JsonProperty("cancel_time")
  public String cancelTime;

  /**
   * Error message if the batch was cancelled by xAI.
   */
  @JsonProperty("cancel_by_xai_message")
  public String cancelByXaiMessage;

  /**
   * Aggregate state information for the batch.
   */
  @JsonProperty("state")
  public BatchState state;

  public String getBatchId() {
    return batchId;
  }

  public void setBatchId(String batchId) {
    this.batchId = batchId;
  }

  public String getCancelByXaiMessage() {
    return cancelByXaiMessage;
  }

  public void setCancelByXaiMessage(String cancelByXaiMessage) {
    this.cancelByXaiMessage = cancelByXaiMessage;
  }

  public String getCancelTime() {
    return cancelTime;
  }

  public void setCancelTime(String cancelTime) {
    this.cancelTime = cancelTime;
  }

  public String getCreateApiKeyId() {
    return createApiKeyId;
  }

  public void setCreateApiKeyId(String createApiKeyId) {
    this.createApiKeyId = createApiKeyId;
  }

  public String getCreateTime() {
    return createTime;
  }

  public void setCreateTime(String createTime) {
    this.createTime = createTime;
  }

  public String getExpireTime() {
    return expireTime;
  }

  public void setExpireTime(String expireTime) {
    this.expireTime = expireTime;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BatchState getState() {
    return state;
  }

  public void setState(BatchState state) {
    this.state = state;
  }

}
