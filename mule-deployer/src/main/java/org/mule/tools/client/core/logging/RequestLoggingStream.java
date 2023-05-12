/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.core.logging;

import static java.lang.String.format;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The goal of this class it to get as a filter for ClientRequestContext. This class will receive data that's about to be send
 * down the pipe and store it however it sees feet so later one someone can retrieve that data and if needed log it or use it with
 * some other purposes.
 * 
 * @author Mulesoft Inc.
 * @since 3.2.0
 */
public class RequestLoggingStream extends FilterOutputStream {

  private final StringBuilder request;
  private final ByteArrayOutputStream requestBody = new ByteArrayOutputStream();

  public RequestLoggingStream(StringBuilder request, OutputStream inner) {
    super(inner);
    this.request = request;
  }

  @Override
  public void write(final int i) throws IOException {
    requestBody.write(i);
    out.write(i);
  }


  public String getRequestLog() {
    request.append(requestBody);
    request.append(format("%n"));
    return request.toString();
  }

}
