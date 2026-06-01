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

import com.xai.client.mgmt.dto.BillingInfo;
import com.xai.client.mgmt.dto.BillingResponse;
import java.net.http.HttpRequest;
import java.util.logging.Logger;

/**
 * Management API Billing Management
 *
 * @author Key Bridge
 * @since v1.0.0 added 2026-06-01
 */
public class XaiBillingClient extends AbstractManagementClient {

  private static final Logger LOG = Logger.getLogger(XaiBillingClient.class.getName());

  private static final String PATH = "/v1/billing/teams/";

  public XaiBillingClient() {
    super(PATH);
  }

  /**
   * Get billing information of the team with given team ID.
   * <p>
   * {@code /v1/billing/teams/{team_id}/billing-info}
   *
   * @param teamId Team ID of the team.
   * @return Billing info response body
   */
  public BillingInfo getBillingInfo(String teamId) {
    String path = teamId + "/billing-info";
    HttpRequest request = doGet(path);
    return sendRequest(request, BillingResponse.class).getBillingInfo();
  }

}
