/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo.standalone;

import org.apache.maven.it.VerificationException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Disabled
public class DomainStandaloneDeploymentTest extends StandaloneDeploymentTest {

  private static final String DOMAIN = "empty-mule-deploy-standalone-domain-project";

  public DomainStandaloneDeploymentTest() {
    super(DOMAIN);
  }

  @Test
  @Timeout(value = 120000, unit = TimeUnit.MILLISECONDS)
  public void standaloneDomainDeployTest() throws IOException, VerificationException, InterruptedException {
    deploy();
    assertThat("Failed to deploy: " + DOMAIN, standaloneEnvironment.isDomainDeployed(DOMAIN), is(true));
  }
}
