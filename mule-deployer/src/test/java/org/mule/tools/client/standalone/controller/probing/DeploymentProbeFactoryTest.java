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
import org.mule.tools.client.standalone.controller.probing.deployment.ApplicationDeploymentProbe;
import org.mule.tools.client.standalone.controller.probing.deployment.DeploymentProbe;
import org.mule.tools.client.standalone.controller.probing.deployment.DomainDeploymentProbe;
import org.mule.tools.client.standalone.controller.probing.deployment.DeploymentProbeFactory;
import org.mule.tools.client.core.exception.DeploymentException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(Parameterized.class)
public class DeploymentProbeFactoryTest {

  public static final String MULE_APPLICATION = "mule-application";
  public static final String MULE_DOMAIN = "mule-domain";
  Set<String> supportedPackaging = newHashSet(MULE_APPLICATION, MULE_DOMAIN);

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {MULE_APPLICATION, ApplicationDeploymentProbe.class}, {MULE_DOMAIN, DomainDeploymentProbe.class},
        {"unsupported-packaging", DeploymentProbe.class}
    });
  }

  private String packaging;

  private Class clazz;

  public DeploymentProbeFactoryTest(String classifier, Class clazz) {
    this.packaging = classifier;
    this.clazz = clazz;
  }

  @Test
  public void createProbeTest() {
    try {
      assertThat("ProbeDeployment is not the expected", DeploymentProbeFactory.createProbe(packaging), new IsInstanceOf(clazz));
    } catch (DeploymentException e) {
      assertThat("The packaging " + packaging + " should have support for probing", !supportedPackaging.contains(packaging),
                 is(true));
    }
  }

}
