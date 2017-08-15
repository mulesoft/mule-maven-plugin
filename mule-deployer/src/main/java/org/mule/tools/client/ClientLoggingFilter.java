/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;

public class ClientLoggingFilter implements ClientRequestFilter, ClientResponseFilter, WriterInterceptor {

  private static final String REQUEST_LOGGING_STREAM = "requestLoggingStream";

  private Log log;

  public ClientLoggingFilter(Log log) {
    this.log = log;
  }

  private void appendHeaders(StringBuilder b, MultivaluedMap<String, String> headers) {
    for (Map.Entry<String, List<String>> headerEntry : headers.entrySet()) {
      b.append(headerEntry.getKey()).append(": ").append(StringUtils.join(headerEntry.getValue(), ", ")).append("\n");
    }
    b.append("\n");
  }


  @Override
  public void filter(ClientRequestContext context) throws IOException {

    StringBuilder request = new StringBuilder();

    request.append("HTTP Request\n");
    request.append(context.getMethod() + " " + context.getUri() + "\n");

    appendHeaders(request, context.getStringHeaders());

    if (context.hasEntity()) {
      OutputStream stream = new RequestLoggingStream(request, context.getEntityStream());
      context.setEntityStream(stream);
      context.setProperty(REQUEST_LOGGING_STREAM, stream);
    } else {
      log.debug(request.toString());
    }
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {

    StringBuilder response = new StringBuilder();

    response.append("HTTP response\n");
    response.append(Integer.toString(responseContext.getStatus())).append(" ")
        .append(responseContext.getStatusInfo().getReasonPhrase()).append("\n");

    appendHeaders(response, responseContext.getHeaders());

    if (responseContext.hasEntity()) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      IOUtils.copy(responseContext.getEntityStream(), stream);
      byte[] responseBytes = stream.toByteArray();
      response.append(new String(responseBytes));
      response.append("\n");
      responseContext.setEntityStream(new ByteArrayInputStream(responseBytes));
    }

    log.debug(response.toString());
  }

  @Override
  public void aroundWriteTo(WriterInterceptorContext writerInterceptorContext) throws IOException, WebApplicationException {
    RequestLoggingStream stream = (RequestLoggingStream) writerInterceptorContext.getProperty(REQUEST_LOGGING_STREAM);

    writerInterceptorContext.proceed();

    if (stream != null) {
      log.debug(stream.getRequestLog());
    }
  }

  private class RequestLoggingStream extends FilterOutputStream {

    private StringBuilder request;
    private ByteArrayOutputStream requestBody = new ByteArrayOutputStream();

    RequestLoggingStream(StringBuilder request, OutputStream inner) {
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
      request.append('\n');
      return request.toString();
    }

  }
}
