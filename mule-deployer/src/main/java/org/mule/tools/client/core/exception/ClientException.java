/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.core.exception;


import javax.ws.rs.core.Response;

public class ClientException extends RuntimeException {

  private int statusCode;
  private String reasonPhrase;

  public ClientException(String message, int statusCode, String reasonPhrase) {
    super(String.format("%d %s: %s", statusCode, reasonPhrase, message));

    this.statusCode = statusCode;
    this.reasonPhrase = reasonPhrase;

  }

  public ClientException(Response response, String uri) {
    this(uri, response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase());
  }

  public ClientException(Response response) {
    this(response.readEntity(String.class), response.getStatusInfo().getStatusCode(), response.getStatusInfo().getReasonPhrase());
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }
}
