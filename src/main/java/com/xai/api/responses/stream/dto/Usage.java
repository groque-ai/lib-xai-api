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

import com.xai.api.responses.stream.dto.CompletionUsageDetail;

/**
 *
 * @author Key Bridge
 */
public class Usage {

  // required
  private int promptTokens;
  private int completionTokens;
  private int totalTokens;
  private PromptUsageDetail promptTokensDetails;
  private CompletionUsageDetail completionTokensDetails;
  private int numSourcesUsed;
  private long costInUsdTicks;

  public int getPromptTokens() {
    return promptTokens;
  }

  public void setPromptTokens(int promptTokens) {
    this.promptTokens = promptTokens;
  }

  public int getCompletionTokens() {
    return completionTokens;
  }

  public void setCompletionTokens(int completionTokens) {
    this.completionTokens = completionTokens;
  }

  public int getTotalTokens() {
    return totalTokens;
  }

  public void setTotalTokens(int totalTokens) {
    this.totalTokens = totalTokens;
  }

  public PromptUsageDetail getPromptTokensDetails() {
    return promptTokensDetails;
  }

  public void setPromptTokensDetails(PromptUsageDetail promptTokensDetails) {
    this.promptTokensDetails = promptTokensDetails;
  }

  public CompletionUsageDetail getCompletionTokensDetails() {
    return completionTokensDetails;
  }

  public void setCompletionTokensDetails(CompletionUsageDetail completionTokensDetails) {
    this.completionTokensDetails = completionTokensDetails;
  }

  public int getNumSourcesUsed() {
    return numSourcesUsed;
  }

  public void setNumSourcesUsed(int numSourcesUsed) {
    this.numSourcesUsed = numSourcesUsed;
  }

  public long getCostInUsdTicks() {
    return costInUsdTicks;
  }

  public void setCostInUsdTicks(long costInUsdTicks) {
    this.costInUsdTicks = costInUsdTicks;
  }
}
