/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.core.logging;


import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.join;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
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

import org.mule.tools.utils.DeployerLog;

public class ClientLoggingFilter implements ClientRequestFilter, ClientResponseFilter, WriterInterceptor {

  public static final String CLIENT_LOGGING_LOG_MULTIPART = "client.logging.log.multipart";

  private static final String REQUEST_LOGGING_STREAM = "requestLoggingStream";

  private final DeployerLog log;

  public ClientLoggingFilter(DeployerLog log) {
    this.log = log;
  }

  @Override
  public void filter(ClientRequestContext context) throws IOException {
    StringBuilder request = new StringBuilder();

    request
        .append("HTTP Request").append(format("%n"))
        .append(context.getMethod()).append(" ").append(context.getUri())
        .append(format("%n"));

    appendHeaders(request, context.getStringHeaders());

    if (context.hasEntity()) {
      if (shouldLogEntity(context.getEntityType())) {
        OutputStream stream = new RequestLoggingStream(request, context.getEntityStream());
        context.setEntityStream(stream);
        context.setProperty(REQUEST_LOGGING_STREAM, stream);
        return;
      }
    }

    log.debug(request.toString());
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
    StringBuilder response = new StringBuilder();

    response
        .append("HTTP response").append(format("%n"))
        .append(responseContext.getStatus()).append(" ").append(responseContext.getStatusInfo().getReasonPhrase())
        .append(format("%n"));

    appendHeaders(response, responseContext.getHeaders());

    if (responseContext.hasEntity()) {
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      IOUtils.copy(responseContext.getEntityStream(), stream);
      byte[] responseBytes = stream.toByteArray();

      response.append(new String(responseBytes)).append(format("%n"));
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

  private void appendHeaders(StringBuilder builder, MultivaluedMap<String, String> headers) {
    for (Map.Entry<String, List<String>> headerEntry : headers.entrySet()) {
      builder.append(headerEntry.getKey()).append(": ").append(join(headerEntry.getValue(), ", ")).append(format("%n"));
    }
    builder.append(format("%n"));
  }

  private boolean shouldLogEntity(Type entityType) {
    if (!entityType.getTypeName().contains("FormDataMultiPart")) {
      return true;
    }

    return entityType.getTypeName().contains("FormDataMultiPart") && shouldLogMultiPart();
  }

  private Boolean shouldLogMultiPart() {
    return Boolean.getBoolean(CLIENT_LOGGING_LOG_MULTIPART);
  }

}
