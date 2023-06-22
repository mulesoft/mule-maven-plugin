/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mule.tools.client.standalone.controller.MuleProcessController.DEFAULT_TIMEOUT;
import static org.mule.tools.client.standalone.controller.MuleProcessController.MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY;

class MuleProcessControllerTest {

  @Test
  void nonParseableMuleControllerTimeoutPropertyTest() {
    System.setProperty(MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY, "abc");
    MuleProcessController controller = new MuleProcessController(StringUtils.EMPTY);
    assertThat(controller.getControllerTimeout()).as("Timeout property is not the expected").isEqualTo(DEFAULT_TIMEOUT);
    System.clearProperty(MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY);
  }

  @Test
  void parseableMuleControllerTimeoutPropertyTest() {
    String validTimeout = "1234";
    System.setProperty(MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY, validTimeout);
    MuleProcessController controller = new MuleProcessController(StringUtils.EMPTY);

    int expectedTimeout = 1234;
    assertThat(controller.getControllerTimeout()).as("Timeout property is not the expected").isEqualTo(expectedTimeout);
    System.clearProperty(MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY);
  }
}
