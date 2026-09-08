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
package com.xai.api.responses.stream;

/**
 *
 * @author Key Bridge
 */
public class CompletionUsageDetail {

  // required
  private int reasoningTokens;
  private int audioTokens;
  private int acceptedPredictionTokens;
  private int rejectedPredictionTokens;

  public int getReasoningTokens() {
    return reasoningTokens;
  }

  public void setReasoningTokens(int reasoningTokens) {
    this.reasoningTokens = reasoningTokens;
  }

  public int getAudioTokens() {
    return audioTokens;
  }

  public void setAudioTokens(int audioTokens) {
    this.audioTokens = audioTokens;
  }

  public int getAcceptedPredictionTokens() {
    return acceptedPredictionTokens;
  }

  public void setAcceptedPredictionTokens(int acceptedPredictionTokens) {
    this.acceptedPredictionTokens = acceptedPredictionTokens;
  }

  public int getRejectedPredictionTokens() {
    return rejectedPredictionTokens;
  }

  public void setRejectedPredictionTokens(int rejectedPredictionTokens) {
    this.rejectedPredictionTokens = rejectedPredictionTokens;
  }
}
