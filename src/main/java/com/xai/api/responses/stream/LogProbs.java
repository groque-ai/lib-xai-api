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

import com.xai.api.responses.output.tokens.TokenLogProb;
import java.util.List;

/**
 *
 * @author Key Bridge
 */
public class LogProbs {

  // nullable: may be null or an array of TokenLogProb
  private List<TokenLogProb> content;

  public List<TokenLogProb> getContent() {
    return content;
  }

  public void setContent(List<TokenLogProb> content) {
    this.content = content;
  }
}
