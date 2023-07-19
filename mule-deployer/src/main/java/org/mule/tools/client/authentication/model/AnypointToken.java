/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.authentication.model;

import java.util.Objects;

/**
 * @author Mulesoft Inc.
 * @since 3.3.0
 */
public class AnypointToken extends AnypointCredential {

  private final String bearerToken;

  public AnypointToken(String bearerToken) {
    Objects.requireNonNull(bearerToken, "Bearer token cannot be null");
    this.bearerToken = bearerToken;
  }

  public String getToken() {
    return this.bearerToken;
  }

  @Override
  public CredentialType credentialType() {
    return CredentialType.token;
  }

}
