/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mule.tools.client.standalone.controller.MuleProcessController.DEFAULT_TIMEOUT;
import static org.mule.tools.client.standalone.controller.MuleProcessController.MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY;

public class MuleProcessControllerTest {

  @Test
  public void nonParseableMuleControllerTimeoutPropertyTest() {
    System.setProperty(MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY, "abc");
    MuleProcessController controller = new MuleProcessController(StringUtils.EMPTY);
    assertThat("Timeout property is not the expected", controller.getControllerTimeout(), equalTo(DEFAULT_TIMEOUT));
    System.clearProperty(MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY);
  }

  @Test
  public void parseableMuleControllerTimeoutPropertyTest() {
    String validTimeout = "1234";
    System.setProperty(MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY, validTimeout);
    MuleProcessController controller = new MuleProcessController(StringUtils.EMPTY);

    int expectedTimeout = 1234;
    assertThat("Timeout property is not the expected", controller.getControllerTimeout(), equalTo(expectedTimeout));
    System.clearProperty(MULE_PROCESS_CONTROLLER_TIMEOUT_PROPERTY);
  }
}
