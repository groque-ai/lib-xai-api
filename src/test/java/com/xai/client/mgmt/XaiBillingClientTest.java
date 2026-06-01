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

import com.xai.client.impl.AbstractServiceImpTest;
import com.xai.client.mgmt.dto.BillingInfo;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author Key Bridge
 */
public class XaiBillingClientTest extends AbstractServiceImpTest {

  public XaiBillingClientTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

//  @Test
  public void testGetBillingInfo() throws IOException {

    String teamId = "8cb69ba9-09d1-4339-986a-b33edbd3819c";

    XaiBillingClient client = new XaiBillingClient();

    BillingInfo response = client.getBillingInfo(teamId);
    printJson(response);

  }

}
