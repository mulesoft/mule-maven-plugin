/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller.probing;

import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class ProbeFactoryTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {"mule-application", AppDeploymentProbe.class}, {"mule-domain", DomainDeploymentProbe.class}
    });
  }

  private String classifier;

  private Class clazz;

  public ProbeFactoryTest(String classifier, Class clazz) {
    this.classifier = classifier;
    this.clazz = clazz;
  }

  @Test
  public void createProbeTest() {
    assertThat("ProbeDeployment is not the expected", ProbeFactory.createProbe(classifier), new IsInstanceOf(clazz));
  }
}
