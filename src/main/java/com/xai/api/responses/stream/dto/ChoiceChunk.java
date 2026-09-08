/*
 * Copyright (C) 2026 Key Bridge. All rights reserved. Use is subject to license
 * terms.
 *
 * This software code is protected by Copyrights and remains the property of
 * Key Bridge. Key Bridge reserves all rights in and to
 * Copyrights and no license is granted under Copyrights in this Software
 * License Agreement.
 *
 * Key Bridge may license Copyrights for commercialization pursuant to
 * the terms of either a Standard Software Source Code License Agreement or a
 * Standard Product License Agreement. A copy of either Agreement can be
 * obtained upon request by sending an email to info@keybridgewireless.com.
 *
 * All information contained herein is the property of Key Bridge.
 * The intellectual and technical concepts contained herein
 * are proprietary.
 */
package com.xai.api.responses.stream.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author Key Bridge
 */
public class ChoiceChunk {

  private int index;               // required
  private Delta delta;             // required

  @JsonProperty("finish_reason")
  private String finishReason;     // nullable
  /**
   * The log probabilities of each output token returned in the content of
   * message.
   */
  private LogProbs logprobs;       // nullable

  public Delta getDelta() {
    return delta;
  }

  public void setDelta(Delta delta) {
    this.delta = delta;
  }

  public String getFinishReason() {
    return finishReason;
  }

  public void setFinishReason(String finishReason) {
    this.finishReason = finishReason;
  }

  public int getIndex() {
    return index;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public LogProbs getLogprobs() {
    return logprobs;
  }

  public void setLogprobs(LogProbs logprobs) {
    this.logprobs = logprobs;
  }

}
