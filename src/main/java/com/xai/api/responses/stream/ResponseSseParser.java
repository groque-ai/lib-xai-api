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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xai.client.exception.ApiParseException;
import java.util.Objects;

/**
 * Assembles SSE lines into {@link ResponseStreamEvent} envelopes.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-07
 */
public class ResponseSseParser {

  public enum Kind {
    NONE,
    EVENT,
    DONE
  }

  public static final class Result {

    private final Kind kind;
    private final ResponseStreamEvent event;

    private Result(Kind kind, ResponseStreamEvent event) {
      this.kind = kind;
      this.event = event;
    }

    public static Result none() {
      return new Result(Kind.NONE, null);
    }

    public static Result done() {
      return new Result(Kind.DONE, null);
    }

    public static Result event(ResponseStreamEvent event) {
      return new Result(Kind.EVENT, event);
    }

    public Kind getKind() {
      return kind;
    }

    public ResponseStreamEvent getEvent() {
      return event;
    }
  }

  private final ObjectMapper mapper;
  private String fieldEvent;
  private final StringBuilder data = new StringBuilder();

  public ResponseSseParser(ObjectMapper mapper) {
    this.mapper = Objects.requireNonNull(mapper, "mapper");
  }

  public Result consumeLine(String line) {
    if (line == null) {
      return finish();
    }
    if (!line.isEmpty() && line.charAt(0) == ':') {
      return Result.none();
    }
    if (line.isEmpty()) {
      return dispatch();
    }
    int colon = line.indexOf(':');
    String field;
    String value;
    if (colon < 0) {
      field = line;
      value = "";
    } else {
      field = line.substring(0, colon);
      value = line.substring(colon + 1);
      if (!value.isEmpty() && value.charAt(0) == ' ') {
        value = value.substring(1);
      }
    }
    if ("event".equals(field)) {
      fieldEvent = value;
    } else if ("data".equals(field)) {
      if (data.length() > 0) {
        data.append('\n');
      }
      data.append(value);
    }
    return Result.none();
  }

  public Result finish() {
    if (data.length() == 0 && fieldEvent == null) {
      return Result.none();
    }
    return dispatch();
  }

  private Result dispatch() {
    if (data.length() == 0 && fieldEvent == null) {
      return Result.none();
    }
    String payload = data.toString();
    data.setLength(0);
    String sseEvent = fieldEvent;
    fieldEvent = null;
    if ("[DONE]".equals(payload.trim())) {
      return Result.done();
    }
    if (payload.isEmpty()) {
      return Result.none();
    }
    try {
      JsonNode node = mapper.readTree(payload);
      String type = null;
      if (node != null && node.hasNonNull("type")) {
        type = node.get("type").asText();
      }
      if (type == null || type.isBlank()) {
        type = sseEvent;
      }
      ResponseEventType event = ResponseEventType.fromName(type);
      ResponseStreamEvent envelope;
      try {
        envelope = mapper.convertValue(node, ResponseStreamEvent.class);
      } catch (IllegalArgumentException ex) {
        // typed fields optional; raw JSON always kept
        envelope = new ResponseStreamEvent();
      }
      if (envelope == null) {
        envelope = new ResponseStreamEvent();
      }
      envelope.setEvent(event);
      envelope.setType(type);
      envelope.setData(node);
      return Result.event(envelope);
    } catch (JsonProcessingException ex) {
      throw new ApiParseException("SSE data JSON error", ex);
    }
  }
}
