/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
