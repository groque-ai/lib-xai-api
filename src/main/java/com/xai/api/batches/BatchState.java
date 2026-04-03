package com.xai.api.batches;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BatchState {

  @JsonProperty("num_cancelled")
  private int numCancelled;

  @JsonProperty("num_error")
  private int numError;

  @JsonProperty("num_pending")
  private int numPending;

  @JsonProperty("num_requests")
  private int numRequests;

  @JsonProperty("num_success")
  private int numSuccess;

  public int getNumCancelled() {
    return numCancelled;
  }

  public void setNumCancelled(int numCancelled) {
    this.numCancelled = numCancelled;
  }

  public int getNumError() {
    return numError;
  }

  public void setNumError(int numError) {
    this.numError = numError;
  }

  public int getNumPending() {
    return numPending;
  }

  public void setNumPending(int numPending) {
    this.numPending = numPending;
  }

  public int getNumRequests() {
    return numRequests;
  }

  public void setNumRequests(int numRequests) {
    this.numRequests = numRequests;
  }

  public int getNumSuccess() {
    return numSuccess;
  }

  public void setNumSuccess(int numSuccess) {
    this.numSuccess = numSuccess;
  }
}
