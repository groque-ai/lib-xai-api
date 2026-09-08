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
 * Wire {@code type} strings for Responses API SSE events.
 *
 * @author Key Bridge
 * @since v1.1.0 created 2026-09-07
 */
public enum ResponseEvent {

  UNKNOWN("unknown"),

  RESPONSE_CREATED("response.created"),
  RESPONSE_IN_PROGRESS("response.in_progress"),
  RESPONSE_QUEUED("response.queued"),
  RESPONSE_COMPLETED("response.completed"),
  RESPONSE_FAILED("response.failed"),
  RESPONSE_INCOMPLETE("response.incomplete"),
  ERROR("error"),

  RESPONSE_OUTPUT_ITEM_ADDED("response.output_item.added"),
  RESPONSE_OUTPUT_ITEM_DONE("response.output_item.done"),
  RESPONSE_CONTENT_PART_ADDED("response.content_part.added"),
  RESPONSE_CONTENT_PART_DONE("response.content_part.done"),

  RESPONSE_OUTPUT_TEXT_DELTA("response.output_text.delta"),
  RESPONSE_OUTPUT_TEXT_DONE("response.output_text.done"),
  RESPONSE_OUTPUT_TEXT_ANNOTATION_ADDED("response.output_text.annotation_added"),
  RESPONSE_TEXT_DELTA("response.text.delta"),
  RESPONSE_TEXT_DONE("response.text.done"),

  RESPONSE_REFUSAL_DELTA("response.refusal.delta"),
  RESPONSE_REFUSAL_DONE("response.refusal.done"),

  RESPONSE_REASONING_TEXT_DELTA("response.reasoning_text.delta"),
  RESPONSE_REASONING_TEXT_DONE("response.reasoning_text.done"),
  RESPONSE_REASONING_SUMMARY_TEXT_DELTA("response.reasoning_summary_text.delta"),
  RESPONSE_REASONING_SUMMARY_TEXT_DONE("response.reasoning_summary_text.done"),
  RESPONSE_REASONING_SUMMARY_PART_ADDED("response.reasoning_summary_part.added"),
  RESPONSE_REASONING_SUMMARY_PART_DONE("response.reasoning_summary_part.done"),

  RESPONSE_FUNCTION_CALL_ARGUMENTS_DELTA("response.function_call_arguments.delta"),
  RESPONSE_FUNCTION_CALL_ARGUMENTS_DONE("response.function_call_arguments.done"),

  RESPONSE_FILE_SEARCH_CALL_IN_PROGRESS("response.file_search_call.in_progress"),
  RESPONSE_FILE_SEARCH_CALL_SEARCHING("response.file_search_call.searching"),
  RESPONSE_FILE_SEARCH_CALL_COMPLETED("response.file_search_call.completed"),

  RESPONSE_WEB_SEARCH_CALL_IN_PROGRESS("response.web_search_call.in_progress"),
  RESPONSE_WEB_SEARCH_CALL_SEARCHING("response.web_search_call.searching"),
  RESPONSE_WEB_SEARCH_CALL_COMPLETED("response.web_search_call.completed"),

  RESPONSE_CODE_INTERPRETER_CALL_IN_PROGRESS("response.code_interpreter_call.in_progress"),
  RESPONSE_CODE_INTERPRETER_CALL_CODE_DELTA("response.code_interpreter_call_code.delta"),
  RESPONSE_CODE_INTERPRETER_CALL_CODE_DONE("response.code_interpreter_call_code.done"),
  RESPONSE_CODE_INTERPRETER_CALL_INTERPRETING("response.code_interpreter_call.interpreting"),
  RESPONSE_CODE_INTERPRETER_CALL_COMPLETED("response.code_interpreter_call.completed"),

  RESPONSE_IMAGE_GENERATION_CALL_IN_PROGRESS("response.image_generation_call.in_progress"),
  RESPONSE_IMAGE_GENERATION_CALL_GENERATING("response.image_generation_call.generating"),
  RESPONSE_IMAGE_GENERATION_CALL_COMPLETED("response.image_generation_call.completed"),
  RESPONSE_IMAGE_GENERATION_CALL_PARTIAL_IMAGE("response.image_generation_call.partial_image"),

  RESPONSE_MCP_CALL_IN_PROGRESS("response.mcp_call.in_progress"),
  RESPONSE_MCP_CALL_COMPLETED("response.mcp_call.completed"),
  RESPONSE_MCP_CALL_FAILED("response.mcp_call.failed"),
  RESPONSE_MCP_CALL_ARGUMENTS_DELTA("response.mcp_call_arguments.delta"),
  RESPONSE_MCP_CALL_ARGUMENTS_DONE("response.mcp_call_arguments.done"),
  RESPONSE_MCP_LIST_TOOLS_IN_PROGRESS("response.mcp_list_tools.in_progress"),
  RESPONSE_MCP_LIST_TOOLS_COMPLETED("response.mcp_list_tools.completed"),
  RESPONSE_MCP_LIST_TOOLS_FAILED("response.mcp_list_tools.failed"),

  RESPONSE_CUSTOM_TOOL_CALL_INPUT_DELTA("response.custom_tool_call_input.delta"),
  RESPONSE_CUSTOM_TOOL_CALL_INPUT_DONE("response.custom_tool_call_input.done"),

  RESPONSE_AUDIO_DELTA("response.audio.delta"),
  RESPONSE_AUDIO_DONE("response.audio.done"),
  RESPONSE_AUDIO_TRANSCRIPT_DELTA("response.audio.transcript.delta"),
  RESPONSE_AUDIO_TRANSCRIPT_DONE("response.audio.transcript.done");

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
   * Reverse lookup by event string. Never null: unmatched, null, or blank
   * values map to {@link #UNKNOWN}.
   */
  public static ResponseEvent fromName(String value) {
    if (value == null) {
      return UNKNOWN;
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      return UNKNOWN;
    }
    for (ResponseEvent event : values()) {
      if (event == UNKNOWN) {
        continue;
      }
      if (event.name.equalsIgnoreCase(trimmed)) {
        return event;
      }
    }
    return UNKNOWN;
  }
}
