/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo.standalone;

import org.apache.maven.it.VerificationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ApplicationStandaloneDeploymentTest extends StandaloneDeploymentTest {

  private static final String APPLICATION = "empty-mule-deploy-standalone-application-project";

  public ApplicationStandaloneDeploymentTest() {
    super(APPLICATION);
  }

  @Test
  @Timeout(value = 60000, unit = TimeUnit.MILLISECONDS)
  public void standaloneApplicationDeploymentTest() throws IOException, VerificationException, InterruptedException {
    deploy();
    assertThat("Failed to deploy: " + APPLICATION, standaloneEnvironment.isDeployed(APPLICATION), is(true));
  }
}
