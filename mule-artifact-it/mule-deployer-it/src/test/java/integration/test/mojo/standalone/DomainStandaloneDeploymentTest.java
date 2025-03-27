/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo.standalone;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@org.junit.jupiter.api.Disabled
public class DomainStandaloneDeploymentTest extends StandaloneDeploymentTest {

  private static final String DOMAIN = "empty-mule-deploy-standalone-domain-project";

  public DomainStandaloneDeploymentTest() {
    super(DOMAIN);
  }

  @Test
  @Timeout(value = 120000, unit = TimeUnit.MILLISECONDS)
  public void standaloneDomainDeployTest() throws Exception {
    deploy();
    assertThat(standaloneEnvironment.isDomainDeployed(DOMAIN)).describedAs("Failed to deploy: " + DOMAIN).isTrue();
  }
}
