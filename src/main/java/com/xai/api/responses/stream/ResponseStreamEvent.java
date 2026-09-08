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

import com.fasterxml.jackson.databind.JsonNode;

/**
 * One Responses SSE JSON object.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-07
 */
public class ResponseStreamEvent {

  private ResponseEvent event;
  private String type;
  private JsonNode data;

  public ResponseStreamEvent() {
  }

  public ResponseStreamEvent(ResponseEvent event, String type, JsonNode data) {
    this.event = event;
    this.type = type;
    this.data = data;
  }

  //<editor-fold defaultstate="collapsed" desc="Accessors">
  public ResponseEvent getEvent() {
    return event;
  }

  public void setEvent(ResponseEvent event) {
    this.event = event;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public JsonNode getData() {
    return data;
  }

  public void setData(JsonNode data) {
    this.data = data;
  }
  //</editor-fold>
}
