/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package integration.test.mojo.standalone;

import org.apache.maven.it.VerificationException;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ApplicationStandaloneDeploymentTest extends StandaloneDeploymentTest {

  private static final String APPLICATION = "empty-mule-deploy-standalone-application-project";

  public ApplicationStandaloneDeploymentTest() {
    super(APPLICATION);
  }

  @Test(timeout = 60000)
  public void standaloneApplicationDeploymentTest() throws IOException, VerificationException, InterruptedException {
    deploy();
    assertThat("Failed to deploy: " + APPLICATION, standaloneEnvironment.isDeployed(APPLICATION), is(true));
  }
}
