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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.xai.api.responses.ModelResponse;
import com.xai.api.responses.output.ModelOutput;
import com.xai.api.responses.output.tokens.Annotation;

/**
 * One Responses SSE JSON object, unmarshaled from the live xAI event shape.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-07
 * @since v1.1.0 update 2026-09-08 typed envelope fields from live captures
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseStreamEvent {

  @JsonIgnore
  private ResponseEvent event;
  private String type;
  @JsonProperty("sequence_number")
  private Integer sequenceNumber;
  private ModelResponse response;
  private ModelOutput item;
  @JsonProperty("output_index")
  private Integer outputIndex;
  @JsonProperty("item_id")
  private String itemId;
  @JsonProperty("content_index")
  private Integer contentIndex;
  @JsonProperty("summary_index")
  private Integer summaryIndex;
  @JsonProperty("annotation_index")
  private Integer annotationIndex;
  private String delta;
  private String text;
  private String arguments;
  private String name;
  private String code;
  private StreamContentPart part;
  private Annotation annotation;
  /**
   * Raw JSON. Always set by the parser; unknown properties live here.
   */
  @JsonIgnore
  private JsonNode data;

  public ResponseStreamEvent() {
  }

  public ResponseStreamEvent(ResponseEvent event, String type, JsonNode data) {
    this.event = event;
    this.type = type;
    this.data = data;
  }

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

  public String getArguments() {
    return arguments;
  }

  public void setArguments(String arguments) {
    this.arguments = arguments;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Integer getContentIndex() {
    return contentIndex;
  }

  public void setContentIndex(Integer contentIndex) {
    this.contentIndex = contentIndex;
  }

  public JsonNode getData() {
    return data;
  }

  public void setData(JsonNode data) {
    this.data = data;
  }

  public String getDelta() {
    return delta;
  }

  public void setDelta(String delta) {
    this.delta = delta;
  }

  public ResponseEvent getEvent() {
    return event;
  }

  public void setEvent(ResponseEvent event) {
    this.event = event;
  }

  public ModelOutput getItem() {
    return item;
  }

  public void setItem(ModelOutput item) {
    this.item = item;
  }

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getOutputIndex() {
    return outputIndex;
  }

  public void setOutputIndex(Integer outputIndex) {
    this.outputIndex = outputIndex;
  }

  public StreamContentPart getPart() {
    return part;
  }

  public void setPart(StreamContentPart part) {
    this.part = part;
  }

  public ModelResponse getResponse() {
    return response;
  }

  public void setResponse(ModelResponse response) {
    this.response = response;
  }

  public Integer getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(Integer sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public Integer getSummaryIndex() {
    return summaryIndex;
  }

  public void setSummaryIndex(Integer summaryIndex) {
    this.summaryIndex = summaryIndex;
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
