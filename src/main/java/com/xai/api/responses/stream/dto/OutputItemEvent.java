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
import com.xai.api.responses.output.ModelOutput;

/**
 * Item family: {@code response.output_item.added} / {@code done}.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
public class OutputItemEvent extends StreamEvent {

  @JsonProperty("output_index")
  private Integer outputIndex;

  private ModelOutput item;

  //<editor-fold defaultstate="collapsed" desc="Accessors">
  public ModelOutput getItem() {
    return item;
  }

  public void setItem(ModelOutput item) {
    this.item = item;
  }

  public Integer getOutputIndex() {
    return outputIndex;
  }

  public void setOutputIndex(Integer outputIndex) {
    this.outputIndex = outputIndex;
  }
  //</editor-fold>
}
