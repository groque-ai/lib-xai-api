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
import com.xai.api.responses.output.tokens.Annotation;

/**
 * Citation: {@code response.output_text.annotation.added} and the
 * underscore alias {@code annotation_added}.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
public class OutputTextAnnotationEvent extends StreamEvent {

  @JsonProperty("item_id")
  private String itemId;

  @JsonProperty("output_index")
  private Integer outputIndex;

  @JsonProperty("content_index")
  private Integer contentIndex;

  @JsonProperty("annotation_index")
  private Integer annotationIndex;

  private Annotation annotation;

  //<editor-fold defaultstate="collapsed" desc="Accessors">
  public Annotation getAnnotation() {
    return annotation;
  }

  public void setAnnotation(Annotation annotation) {
    this.annotation = annotation;
  }

  public Integer getAnnotationIndex() {
    return annotationIndex;
  }

  public void setAnnotationIndex(Integer annotationIndex) {
    this.annotationIndex = annotationIndex;
  }

  public Integer getContentIndex() {
    return contentIndex;
  }

  public void setContentIndex(Integer contentIndex) {
    this.contentIndex = contentIndex;
  }

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
  //</editor-fold>
}
