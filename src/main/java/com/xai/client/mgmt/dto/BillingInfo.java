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
package com.xai.client.mgmt.dto;

/**
 *
 * @author Key Bridge
 */
public class BillingInfo {

  /**
   * Full legal name of the billing contact
   */
  private String name;

  /**
   * Nested postal address for billing
   */
  private BillingAddress address;

  /**
   * Contact email for billing notifications
   */
  private String email;

  /**
   * Tax ID type (e.g., EIN, VAT, GST)
   */
  private String taxIdType;

  /**
   * Tax identification number
   */
  private String taxNumber;

  public BillingAddress getAddress() {
    return address;
  }

  public void setAddress(BillingAddress address) {
    this.address = address;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTaxIdType() {
    return taxIdType;
  }

  public void setTaxIdType(String taxIdType) {
    this.taxIdType = taxIdType;
  }

  public String getTaxNumber() {
    return taxNumber;
  }

  public void setTaxNumber(String taxNumber) {
    this.taxNumber = taxNumber;
  }

}
