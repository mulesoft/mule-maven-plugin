/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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

