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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.xai.api.responses.stream.ResponseEventType;

/**
 * One Responses SSE {@code data:} object. {@code type} is the Jackson
 * discriminator; several wire names share a class when the JSON keys match.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-08
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "type",
  visible = true,
  defaultImpl = UnknownStreamEvent.class)
@JsonSubTypes({
  @JsonSubTypes.Type(value = SnapshotEvent.class, name = "response.created"),
  @JsonSubTypes.Type(value = SnapshotEvent.class, name = "response.in_progress"),
  @JsonSubTypes.Type(value = SnapshotEvent.class, name = "response.completed"),
  @JsonSubTypes.Type(value = SnapshotEvent.class, name = "response.queued"),
  @JsonSubTypes.Type(value = SnapshotEvent.class, name = "response.failed"),
  @JsonSubTypes.Type(value = SnapshotEvent.class, name = "response.incomplete"),
  @JsonSubTypes.Type(value = OutputItemEvent.class, name = "response.output_item.added"),
  @JsonSubTypes.Type(value = OutputItemEvent.class, name = "response.output_item.done"),
  @JsonSubTypes.Type(value = ContentPartEvent.class, name = "response.content_part.added"),
  @JsonSubTypes.Type(value = ContentPartEvent.class, name = "response.content_part.done"),
  @JsonSubTypes.Type(value = ReasoningSummaryPartEvent.class, name = "response.reasoning_summary_part.added"),
  @JsonSubTypes.Type(value = ReasoningSummaryPartEvent.class, name = "response.reasoning_summary_part.done"),
  @JsonSubTypes.Type(value = OutputTextDeltaEvent.class, name = "response.output_text.delta"),
  @JsonSubTypes.Type(value = OutputTextDeltaEvent.class, name = "response.text.delta"),
  @JsonSubTypes.Type(value = OutputTextDoneEvent.class, name = "response.output_text.done"),
  @JsonSubTypes.Type(value = OutputTextDoneEvent.class, name = "response.text.done"),
  @JsonSubTypes.Type(value = OutputTextAnnotationEvent.class, name = "response.output_text.annotation.added"),
  @JsonSubTypes.Type(value = OutputTextAnnotationEvent.class, name = "response.output_text.annotation_added"),
  @JsonSubTypes.Type(value = ReasoningSummaryTextDeltaEvent.class, name = "response.reasoning_summary_text.delta"),
  @JsonSubTypes.Type(value = ReasoningSummaryTextDoneEvent.class, name = "response.reasoning_summary_text.done"),
  @JsonSubTypes.Type(value = IndexedDeltaEvent.class, name = "response.function_call_arguments.delta"),
  @JsonSubTypes.Type(value = IndexedDeltaEvent.class, name = "response.code_interpreter_call_code.delta"),
  @JsonSubTypes.Type(value = FunctionCallArgumentsDoneEvent.class, name = "response.function_call_arguments.done"),
  @JsonSubTypes.Type(value = CodeInterpreterCodeDoneEvent.class, name = "response.code_interpreter_call_code.done"),
  @JsonSubTypes.Type(value = ToolPhaseEvent.class, name = "response.web_search_call.in_progress"),
  @JsonSubTypes.Type(value = ToolPhaseEvent.class, name = "response.web_search_call.searching"),
  @JsonSubTypes.Type(value = ToolPhaseEvent.class, name = "response.web_search_call.completed"),
  @JsonSubTypes.Type(value = ToolPhaseEvent.class, name = "response.code_interpreter_call.in_progress"),
  @JsonSubTypes.Type(value = ToolPhaseEvent.class, name = "response.code_interpreter_call.interpreting"),
  @JsonSubTypes.Type(value = ToolPhaseEvent.class, name = "response.code_interpreter_call.completed"),
  @JsonSubTypes.Type(value = ErrorEvent.class, name = "error")
})
public abstract class StreamEvent {

  @JsonProperty("sequence_number")
  protected Integer sequenceNumber;

  protected String type;

  /**
   * Enumerated form of {@link #type}. Unknown names map to
   * {@link ResponseEventType#UNKNOWN}.
   */
  @JsonIgnore
  public ResponseEventType getEvent() {
    return ResponseEventType.fromName(type);
  }

  //<editor-fold defaultstate="collapsed" desc="Accessors">
  public Integer getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(Integer sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
  //</editor-fold>
}
