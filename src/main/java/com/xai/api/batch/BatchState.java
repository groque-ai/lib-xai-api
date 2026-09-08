package com.xai.api.batch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds aggregate information about the current state of a batch process.
 */
public class BatchState {

  /**
   * Total number of requests in the batch.
   */
  @JsonProperty("num_requests")
  public int numRequests;

  /**
   * Total number of pending requests.
   */
  @JsonProperty("num_pending")
  public int numPending;

  /**
   * Total number of successful requests.
   */
  @JsonProperty("num_success")
  public int numSuccess;

  /**
   * Total number of requests that finished with an error.
   */
  @JsonProperty("num_error")
  public int numError;

  /**
   * Total number of cancelled requests.
   */
  @JsonProperty("num_cancelled")
  public int numCancelled;

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
