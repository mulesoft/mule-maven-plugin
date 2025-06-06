/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.core.exception;

import javax.ws.rs.core.Response;

public class ClientException extends RuntimeException {

  private final int statusCode;
  private final String reasonPhrase;

  public ClientException(String message, int statusCode, String reasonPhrase) {
    super(String.format("%d %s: %s", statusCode, reasonPhrase, message));

    this.statusCode = statusCode;
    this.reasonPhrase = reasonPhrase;

  }

  public ClientException(Response response, String message) {
    this(message, response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase());
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }
}
