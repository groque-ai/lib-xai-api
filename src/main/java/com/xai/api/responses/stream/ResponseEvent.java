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

/**
 * Enumerates all response event types emitted by the system. Each enum constant
 * preserves the original event string as its {@code name} value.
 */
public enum ResponseEvent {

  RESPONSE_CREATED("response.created"),
  RESPONSE_IN_PROGRESS("response.in_progress"),
  RESPONSE_FAILED("response.failed"),
  RESPONSE_COMPLETED("response.completed"),
  RESPONSE_OUTPUT_ITEM_ADDED("response.output_item.added"),
  RESPONSE_OUTPUT_ITEM_DONE("response.output_item.done"),
  RESPONSE_CONTENT_PART_ADDED("response.content_part.added"),
  RESPONSE_CONTENT_PART_DONE("response.content_part.done"),
  RESPONSE_OUTPUT_TEXT_DELTA("response.output_text.delta"),
  RESPONSE_OUTPUT_TEXT_ANNOTATION_ADDED("response.output_text.annotation_added"),
  RESPONSE_TEXT_DONE("response.text.done"),
  RESPONSE_REFUSAL_DELTA("response.refusal.delta"),
  RESPONSE_REFUSAL_DONE("response.refusal.done"),
  RESPONSE_FUNCTION_CALL_ARGUMENTS_DELTA("response.function_call_arguments.delta"),
  RESPONSE_FUNCTION_CALL_ARGUMENTS_DONE("response.function_call_arguments.done"),
  RESPONSE_FILE_SEARCH_CALL_IN_PROGRESS("response.file_search_call.in_progress"),
  RESPONSE_FILE_SEARCH_CALL_SEARCHING("response.file_search_call.searching"),
  RESPONSE_FILE_SEARCH_CALL_COMPLETED("response.file_search_call.completed"),
  RESPONSE_CODE_INTERPRETER_IN_PROGRESS("response.code_interpreter.in_progress"),
  RESPONSE_CODE_INTERPRETER_CALL_CODE_DELTA("response.code_interpreter.call.code_delta"),
  RESPONSE_CODE_INTERPRETER_CALL_CODE_DONE("response.code_interpreter.call.code_done"),
  RESPONSE_CODE_INTERPRETER_CALL_INTERPRETING("response.code_interpreter.call.interpreting"),
  RESPONSE_CODE_INTERPRETER_CALL_COMPLETED("response.code_interpreter.call.completed"),
  ERROR("error");

  private final String name;

  ResponseEvent(String name) {
    this.name = name;
  }

  /**
   * Returns the canonical event string associated with this enum constant.
   */
  public String getName() {
    return name;
  }

  /**
   * Reverse lookup by event string. Returns null if no matching event is found.
   */
  public static ResponseEvent fromName(String value) {
    if (value == null) {
      return null;
    }
    for (ResponseEvent event : values()) {
      if (event.name.equalsIgnoreCase(value.trim())) {
        return event;
      }
    }
    return null;
  }
}
