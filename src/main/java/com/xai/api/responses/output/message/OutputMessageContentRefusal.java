package com.xai.api.responses.output.message;

import com.xai.api.type.OutputMessageContentType;

public class OutputMessageContentRefusal extends OutputMessageContent {

  // Reason for refusal
  private String refusal;

  public OutputMessageContentRefusal() {
    super(OutputMessageContentType.refusal);
  }

  public String getRefusal() {
    return refusal;
  }

  public void setRefusal(String refusal) {
    this.refusal = refusal;
  }
}
