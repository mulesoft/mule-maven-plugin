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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClientExceptionTest {

  private static final String URI = "/var/log/httpd/error_log";
  private static final int STATUS_CODE = 500;
  private static final String REASON_PHRASE = "Internal Server Error";
  private static final ClientException CUSTOM_CLIENT_EXCEPTION = new ClientException(URI, STATUS_CODE, REASON_PHRASE);
  private final Response responseMock = mock(Response.class);
  private final Response.StatusType statusTypeMock = mock(Response.StatusType.class);

  @BeforeEach
  void setUp() {
    when(statusTypeMock.getReasonPhrase()).thenReturn(REASON_PHRASE);
    when(statusTypeMock.getStatusCode()).thenReturn(STATUS_CODE);
    when(responseMock.getStatusInfo()).thenReturn(statusTypeMock);
    when(responseMock.readEntity(String.class)).thenReturn(URI);
  }

  @Test
  void clientExceptionCustomMessageTest() {
    testException(CUSTOM_CLIENT_EXCEPTION);
  }

  @Test
  void clientExceptionResponseMockAndUriTest() {
    testException(new ClientException(responseMock, URI));
  }

  @Test
  void clientExceptionResponseMockTest() {
    testException(new ClientException(responseMock));
  }

  @Test
  void getStatusCodeTest() {
    assertThat(CUSTOM_CLIENT_EXCEPTION.getStatusCode()).as("Status code is not the expected").isEqualTo(STATUS_CODE);
  }

  private void testException(Throwable throwable) {
    assertThatThrownBy(() -> {
      throw throwable;
    })
        .isExactlyInstanceOf(ClientException.class)
        .hasMessage(CUSTOM_CLIENT_EXCEPTION.getMessage());
  }
}
