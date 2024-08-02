/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import org.apache.maven.shared.verifier.Verifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mule.tools.client.OperationRetrier;
import org.mule.tools.client.OperationRetrier.RetriableOperation;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.model.anypoint.CloudHubDeployment;

import java.util.stream.Stream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.*;
import static org.mule.tools.client.AbstractMuleClient.DEFAULT_BASE_URL;

public class CloudHubDeploymentTest extends AbstractDeploymentTest {

  private static final long RETRY_SLEEP_TIME = 30000;
  private static final int APPLICATION_NAME_LENGTH = 10;
  private static final String DEPLOYMENT_TIMEOUT = "1000000";
  private static final String STARTED_STATUS = "STARTED";

  private static Stream<Arguments> muleVersions() {
    return Stream.of(Arguments.of("4.7"));
  }

  private Verifier verifier;
  private String application;
  private String applicationName;

  public String getApplication() {
    return application;
  }

  public void before(String muleVersion, String application) throws Exception {
    LOG.info("Initializing context...");
    this.application = application;
    this.applicationName = randomAlphabetic(APPLICATION_NAME_LENGTH).toLowerCase();
    verifier = buildBaseVerifier();
    verifier.setSystemProperty("username", getUsername());
    verifier.setSystemProperty("password", getPassword());
    verifier.setSystemProperty("environment", PRODUCTION_ENVIRONMENT);
    verifier.setSystemProperty("mule.version", muleVersion);
    verifier.setSystemProperty("cloudhub.application.name", applicationName);
    verifier.setSystemProperty("cloudhub.deployment.timeout", DEPLOYMENT_TIMEOUT);
  }

  @ParameterizedTest
  @MethodSource("muleVersions")
  void testCloudHubDeploy(String muleVersion) throws Exception {
    before(muleVersion, "empty-mule-deploy-cloudhub-project");
    CloudHubClient cloudHubClient = new CloudHubClient(getCloudhubDeployment(), null);

    LOG.info("Executing mule:deploy goal...");
    verifier.addCliArguments(DEPLOY_GOAL, "-DmuleDeploy");
    verifier.execute();

    String status = validateApplicationIsInStatus(cloudHubClient, applicationName, STARTED_STATUS);
    assertThat(status).isEqualTo(STARTED_STATUS).describedAs("Application was not deployed");

    verifier.verifyErrorFreeLog();
  }

  @ParameterizedTest
  @MethodSource("muleVersions")
  void testCloudHubDeployWithInvalidOrg(String muleVersion) throws Exception {
    before(muleVersion, "empty-mule-deploy-cloudhub-invalid-group-project");
    assertThatThrownBy(() -> {
      LOG.info("Executing mule:deploy goal...");
      verifier.addCliArguments(DEPLOY_GOAL, "-DmuleDeploy");
      verifier.execute();
    }).hasMessageContaining("java.lang.IllegalStateException: Cannot get the environment, the business group is not valid");
  }

  private CloudHubDeployment getCloudhubDeployment() {
    CloudHubDeployment cloudHubDeployment = new CloudHubDeployment();
    cloudHubDeployment.setUsername(getUsername());
    cloudHubDeployment.setPassword(getPassword());
    cloudHubDeployment.setUri(DEFAULT_BASE_URL);
    cloudHubDeployment.setEnvironment(PRODUCTION_ENVIRONMENT);
    return cloudHubDeployment;
  }

  private String validateApplicationIsInStatus(CloudHubClient cloudHubClient, String applicationName, String status)
      throws Exception {
    LOG.debug("Checking application " + applicationName + " for status " + status + "...");

    ApplicationStatusRetriableOperation operation =
        new ApplicationStatusRetriableOperation(status, applicationName, cloudHubClient);

    OperationRetrier operationRetrier = new OperationRetrier();
    operationRetrier.setSleepTime(RETRY_SLEEP_TIME);

    operationRetrier.retry(operation);

    return operation.getApplicationStatus();
  }

  private static class ApplicationStatusRetriableOperation implements RetriableOperation {

    private String applicationStatus;

    private final String expectedStatus;
    private final String applicationName;
    private final CloudHubClient cloudHubClient;

    public ApplicationStatusRetriableOperation(String expectedStatus, String applicationName, CloudHubClient cloudHubClient) {
      this.expectedStatus = expectedStatus;
      this.applicationName = applicationName;
      this.cloudHubClient = cloudHubClient;
    }

    public String getApplicationStatus() {
      return applicationStatus;
    }

    @Override
    public Boolean run() {
      Application application = cloudHubClient.getApplications(applicationName);
      if (application != null) {
        applicationStatus = application.getStatus();
        return !expectedStatus.equals(application.getStatus());
      }
      return true;
    }
  }
}
