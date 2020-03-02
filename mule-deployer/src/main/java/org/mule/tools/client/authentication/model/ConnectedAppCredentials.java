/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.authentication.model;

/**
 * @author Mulesoft Inc.
 * @since 3.4.0
 */
public class ConnectedAppCredentials extends AnypointCredential {

  private final String clientId;
  private final String clientSecret;
  private final String grantType;


  public ConnectedAppCredentials(String clientId, String clientSecret, String grantType) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.grantType = grantType;
  }


  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getGrantType() {
    return grantType;
  }

  public CredentialType credentialType() {
    return CredentialType.connectedApp;
  }
}

