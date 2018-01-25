/*
 * Copyright (c) 2015 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.tools.client.core.logging;

import static java.lang.String.format;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Mulesoft Inc.
 * @since 3.2.0
 */
public class RequestLoggingStream extends FilterOutputStream {

  private StringBuilder request;
  private ByteArrayOutputStream requestBody = new ByteArrayOutputStream();

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
    request.append(new String(requestBody.toByteArray()));
    request.append(format("%n"));
    return request.toString();
  }

}
