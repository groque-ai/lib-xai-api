package com.xai.api.batches;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <pre>
 * {
 * "batch_id": "batch_1934e8b5-f3dc-45f1-8329-9841b0aee9d8",
 * "name": "My New Batch",
 * "create_time": "2025-11-11",
 * "expire_time": "2025-11-12",
 * "create_api_key_id": "********-****-****-****-************",
 * "cancel_time": null,
 * "cancel_by_xai_message": null,
 * "state": {
 * "num_requests": 0,
 * "num_pending": 0,
 * "num_success": 0,
 * "num_error": 0,
 * "num_cancelled": 0
 * }
 * }
 * </pre>
 *
 * @author Key Bridge
 */
public class BatchResponse {

  @JsonProperty("batch_id")
  private String batchId;

  @JsonProperty("create_api_key_id")
  private String createApiKeyId;

  @JsonProperty("create_time")
  private String createTime;

  @JsonProperty("name")
  private String name;

  @JsonProperty("state")
  private BatchState state;

  @JsonProperty("cancel_by_xai_message")
  private String cancelByXaiMessage;

  @JsonProperty("cancel_time")
  private String cancelTime;

  @JsonProperty("expire_time")
  private String expireTime;

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
