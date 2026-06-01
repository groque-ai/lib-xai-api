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
package com.xai.client.mgmt;

import com.xai.client.XaiAbstractClient;
import java.net.URI;
import java.net.http.HttpRequest;

/**
 *
 * @author Key Bridge
 */
public abstract class AbstractManagementClient extends XaiAbstractClient {

  /**
   * The Management API allows you to perform operations on your team
   * programmatically. You need a management key in order to use this API. The
   * base URL for all endpoints is https://management-api.x.ai.
   * <p>
   * The Management API serves as a dedicated interface to the xAI platform,
   * empowering developers and teams to programmatically manage their xAI API
   * teams.
   * <p>
   * For example, users can provision their API key, handle access controls, and
   * perform team-level operations like creating, listing, updating, or deleting
   * keys and associated access control lists (ACLs). This API also facilitates
   * oversight of billing aspects, including monitoring prepaid credit balances
   * and usage deductions, ensuring seamless scalability and cost transparency
   * for Grok model integrations.
   * <p>
   * To get started, go to xAI Console. On users page, make sure your xAI
   * account has Management Keys Read + Write permission, and obtain your
   * Management API key on the settings page. If you don't see any of these
   * options, please ask your team administrator to enable the appropriate
   * permissions.
   *
   * @see https://docs.x.ai/developers/rest-api-reference/management
   */
  protected static final String MGMT_BASE_URI = "https://management-api.x.ai";

  public AbstractManagementClient(String path) {
    super(path);
    this.baseUrl = MGMT_BASE_URI + path; // rewrite the base uri
  }

  /**
   * Creates a base HTTP request builder with common headers.
   *
   * @param path the endpoint path
   * @return the request builder
   */
  @Override
  protected HttpRequest.Builder buildRequest(String path) {
    // same as parent but substitutes in the management key
    return HttpRequest.newBuilder()
      .uri(URI.create(baseUrl + path))
      .header("Authorization", "Bearer " + config.getManagementKey())
      .header("Content-Type", "application/json")
      .header("Accept", "application/json");
  }

}
