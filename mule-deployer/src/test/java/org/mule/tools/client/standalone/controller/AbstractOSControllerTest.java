/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

import org.apache.commons.exec.*;
import org.mule.tools.client.standalone.exception.MuleControllerException;

class AbstractOSControllerTest {


  private final String muleHome = UUID.randomUUID().toString();
  private final Integer timeout = 10000;

  private final AbstractOSController controller = new AbstractOSController(muleHome, timeout) {

    @Override
    public String getMuleBin() {
      return muleHome + "/bin";
    }

    @Override
    public int status(String... args) {
      return 0;
    }

    @Override
    public int getProcessId() {
      return 0;
    }
  };

  @Test
  void getMuleHomeTest() {
    assertThat(muleHome).isEqualTo(controller.getMuleHome());
    assertThat(controller.getMuleBin()).contains(controller.getMuleHome());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2})
  void startTest(int index) {
    try (
        MockedConstruction<DefaultExecutor> defaultExecutor = Mockito.mockConstruction(DefaultExecutor.class, (mock, context) -> {
          if (index == 1) {
            when(mock.execute(Mockito.any(CommandLine.class), anyMap())).thenThrow(new IOException());
          }
          if (index == 2) {
            when(mock.execute(Mockito.any(CommandLine.class), anyMap())).thenThrow(new ExecuteException("BLABLA", 123));
          }
        });
        MockedConstruction<ExecuteWatchdog> executeWatchdog = Mockito.mockConstruction(ExecuteWatchdog.class, (mock, context) -> {

        });) {

      if (index == 0) {
        controller.start("start", "ok");
        controller.restart("restart", "ok");
        controller.stop("stop", "ok");
      } else {
        assertThatThrownBy(() -> controller.start("start", "ok")).isInstanceOf(MuleControllerException.class);
        assertThatThrownBy(() -> controller.restart("restart", "ok")).isInstanceOf(MuleControllerException.class);

      }
    }
  }
}
