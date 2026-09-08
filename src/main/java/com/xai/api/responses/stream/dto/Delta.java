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
import java.util.List;

/**
 *
 * @author Key Bridge
 */
public class Delta {

  private String content;                  // nullable
  private List<String> images;             // nullable
  @JsonProperty("reasoning_content")
  private String reasoningContent;         // nullable
  private String role;                     // nullable
  @JsonProperty("tool_calls")
  private List<ToolCall> toolCalls;        // nullable

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public List<String> getImages() {
    return images;
  }

  public void setImages(List<String> images) {
    this.images = images;
  }

  public String getReasoningContent() {
    return reasoningContent;
  }

  public void setReasoningContent(String reasoningContent) {
    this.reasoningContent = reasoningContent;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public List<ToolCall> getToolCalls() {
    return toolCalls;
  }

  public void setToolCalls(List<ToolCall> toolCalls) {
    this.toolCalls = toolCalls;
  }

}
