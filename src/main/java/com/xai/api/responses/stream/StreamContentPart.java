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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.xai.api.responses.output.tokens.Annotation;
import java.util.List;

/**
 * {@code part} payload on content_part and reasoning_summary_part events.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StreamContentPart {

  private String type;
  private String text;
  private List<Object> logprobs;
  private List<Annotation> annotations;

  //<editor-fold defaultstate="collapsed" desc="Accessors">
  public List<Annotation> getAnnotations() {
    return annotations;
  }

  public void setAnnotations(List<Annotation> annotations) {
    this.annotations = annotations;
  }

  public List<Object> getLogprobs() {
    return logprobs;
  }

  public void setLogprobs(List<Object> logprobs) {
    this.logprobs = logprobs;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
  //</editor-fold>
}
