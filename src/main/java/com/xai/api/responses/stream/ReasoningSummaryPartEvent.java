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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xai.api.responses.output.reasoning.ReasoningText;

/**
 * Reasoning-summary part family: {@code reasoning_summary_part.added} /
 * {@code done}. {@code part.type} on the wire is {@code summary_text}.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
public class ReasoningSummaryPartEvent extends StreamEvent {

  @JsonProperty("item_id")
  private String itemId;

  @JsonProperty("output_index")
  private Integer outputIndex;

  @JsonProperty("summary_index")
  private Integer summaryIndex;

  private ReasoningText part;

  //<editor-fold defaultstate="collapsed" desc="Accessors">
  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public Integer getOutputIndex() {
    return outputIndex;
  }

  public void setOutputIndex(Integer outputIndex) {
    this.outputIndex = outputIndex;
  }

  public ReasoningText getPart() {
    return part;
  }

  public void setPart(ReasoningText part) {
    this.part = part;
  }

  public Integer getSummaryIndex() {
    return summaryIndex;
  }

  public void setSummaryIndex(Integer summaryIndex) {
    this.summaryIndex = summaryIndex;
  }
  //</editor-fold>
}
