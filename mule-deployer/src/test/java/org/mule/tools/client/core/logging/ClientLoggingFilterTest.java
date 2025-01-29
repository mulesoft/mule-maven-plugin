/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.core.logging;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.WriterInterceptorContext;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mule.tools.utils.DeployerLog;

class ClientLoggingFilterTest {

  @Mock
  private ClientRequestContext requestContext;
  @Mock
  private ClientResponseContext responseContext;
  @Mock
  private MultivaluedMap<String, String> headers;
  @Mock
  private DeployerLog log;

  private ClientLoggingFilter filter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    filter = new ClientLoggingFilter(log);
  }

  @Test
  void filterRequestLoggingTest() {
    when(requestContext.getMethod()).thenReturn("GET");
    when(requestContext.getUri()).thenReturn(URI.create("http://example.com"));
    when(requestContext.getStringHeaders()).thenReturn(headers);
    when(requestContext.hasEntity()).thenReturn(false);

    filter.filter(requestContext);

    verify(log).debug(contains("HTTP Request"));
  }

  @Test
  void filterResponseLoggingTest() throws IOException {

    when(responseContext.getStatus()).thenReturn(200);
    Response.StatusType statusTypeMock = mock(Response.StatusType.class);
    when(responseContext.getStatusInfo()).thenReturn(statusTypeMock);
    when(statusTypeMock.getReasonPhrase()).thenReturn("OK");

    when(responseContext.getHeaders()).thenReturn(headers);
    when(responseContext.hasEntity()).thenReturn(true);

    ByteArrayInputStream entityStream = new ByteArrayInputStream("response body".getBytes());
    when(responseContext.getEntityStream()).thenReturn(entityStream);

    filter.filter(requestContext, responseContext);

    verify(log).debug(contains("HTTP response"));
  }

  @Test
  void filterRequestLoggingWithEntityTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    ClientRequestContext context = mock(ClientRequestContext.class);
    DeployerLog log = mock(DeployerLog.class);
    ClientLoggingFilter filter = new ClientLoggingFilter(log);

    when(context.getMethod()).thenReturn("POST");
    when(context.getUri()).thenReturn(URI.create("http://example.com"));
    when(context.getStringHeaders()).thenReturn(headers);
    when(context.hasEntity()).thenReturn(true);
    when(context.getEntityType()).thenReturn(String.class);

    filter.filter(context);

    verify(context).setEntityStream(any(OutputStream.class));
    verify(context).setProperty(eq("requestLoggingStream"), any(OutputStream.class));
  }


  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  void shouldLogEntityTest(int index) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    System.setProperty(ClientLoggingFilter.CLIENT_LOGGING_LOG_MULTIPART, "true");
    Object result = null;
    ClientLoggingFilter filter = new ClientLoggingFilter(log);

    Method method = ClientLoggingFilter.class.getDeclaredMethod("shouldLogEntity", Type.class);
    method.setAccessible(true);

    if (index == 0) {
      Type entityType = String.class;
      result = method.invoke(filter, entityType);
    } else if (index == 1) {
      Type entityType = FormDataMultiPart.class;
      result = method.invoke(filter, entityType);
    }

    assertTrue((Boolean) result);
  }

  @Test
  void ShouldLogExceptionTest() throws Exception {
    System.setProperty(ClientLoggingFilter.CLIENT_LOGGING_LOG_MULTIPART, "true");

    ClientLoggingFilter filter = new ClientLoggingFilter(log);

    Method method = ClientLoggingFilter.class.getDeclaredMethod("shouldLogMultiPart");
    method.setAccessible(true);

    Boolean result = (Boolean) method.invoke(filter);
    assertTrue(result);
  }

  @Test
  void aroundWriteToTest() throws IOException, WebApplicationException {
    WriterInterceptorContext writerInterceptorContext = mock(WriterInterceptorContext.class);

    RequestLoggingStream mockStream = mock(RequestLoggingStream.class);
    when(mockStream.getRequestLog()).thenReturn("request log");
    when(writerInterceptorContext.getProperty("requestLoggingStream")).thenReturn(mockStream);
    doNothing().when(writerInterceptorContext).proceed();

    DeployerLog log = mock(DeployerLog.class);
    ClientLoggingFilter filter = new ClientLoggingFilter(log);

    filter.aroundWriteTo(writerInterceptorContext);

    verify(log).debug("request log");
  }
}
