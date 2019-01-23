/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.authentication.model;


/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class Credentials extends AnypointCredential {

  private final String username;
  private final String password;

  public Credentials(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  /**
   * @since 3.3.0
   */
  @Override
  public CredentialType credentialType() {
    return CredentialType.user;
  }



}
