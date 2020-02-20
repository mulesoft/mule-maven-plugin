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

  private final String client_id;
  private final String client_secret;
  private final String grant_type;


  public ConnectedAppCredentials(String clientId, String clientSecret, String grantType) {
    this.client_id = clientId;
    this.client_secret = clientSecret;
    this.grant_type = grantType;
  }


  public String getClient_id() {
    return client_id;
  }

  public String getClient_secret() {
    return client_secret;
  }

  public String getGrant_type() {
    return grant_type;
  }

  public CredentialType credentialType() {
    return CredentialType.connectedApp;
  }
}

