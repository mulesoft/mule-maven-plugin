/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.core.exception;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientExceptionTest {

  private static final String URI = "/var/log/httpd/error_log";
  private static final int STATUS_CODE = 500;
  private static final String REASON_PHRASE = "Internal Server Error";
  private static final ClientException CUSTOM_CLIENT_EXCEPTION = new ClientException(URI, STATUS_CODE, REASON_PHRASE);
  private Response responseMock = mock(Response.class);
  private Response.StatusType statusTypeMock = mock(Response.StatusType.class);

  @Rule
  public ExpectedException expected = ExpectedException.none();

  @Before
  public void setUp() {
    when(statusTypeMock.getReasonPhrase()).thenReturn(REASON_PHRASE);
    when(statusTypeMock.getStatusCode()).thenReturn(STATUS_CODE);
    when(responseMock.getStatusInfo()).thenReturn(statusTypeMock);
    when(responseMock.readEntity(String.class)).thenReturn(URI);
  }

  @Test
  public void clientExceptionCustomMessageTest() {
    setUpException();
    throw CUSTOM_CLIENT_EXCEPTION;
  }

  @Test
  public void clientExceptionResponseMockAndUriTest() {
    setUpException();
    throw new ClientException(responseMock, URI);
  }

  @Test
  public void clientExceptionResponseMockTest() {
    setUpException();
    throw new ClientException(responseMock);
  }

  @Test
  public void getStatusCodeTest() {
    assertThat("Status code is not the expected", CUSTOM_CLIENT_EXCEPTION.getStatusCode(), equalTo(STATUS_CODE));
  }

  private void setUpException() {
    expected.expect(ClientException.class);
    expected.expectMessage(CUSTOM_CLIENT_EXCEPTION.getMessage());
  }

}
