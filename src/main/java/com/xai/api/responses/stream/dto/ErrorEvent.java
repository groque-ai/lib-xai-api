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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Stream error frame ({@code type=error}). {@code error} stays a node so we
 * do not invent a one-off error POJO.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
public class ErrorEvent extends StreamEvent {

  private Integer status;

  private JsonNode error;

  //<editor-fold defaultstate="collapsed" desc="Accessors">
  public JsonNode getError() {
    return error;
  }

  public void setError(JsonNode error) {
    this.error = error;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }
  //</editor-fold>
}
