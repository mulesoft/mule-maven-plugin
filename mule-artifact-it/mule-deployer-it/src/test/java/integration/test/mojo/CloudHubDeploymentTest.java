/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mule.tools.client.OperationRetrier;
import org.mule.tools.client.OperationRetrier.RetriableOperation;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.model.anypoint.CloudHubDeployment;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mule.tools.client.AbstractMuleClient.DEFAULT_BASE_URL;

@Disabled
public class CloudHubDeploymentTest extends AbstractDeploymentTest implements TestWatcher {

  private static final long RETRY_SLEEP_TIME = 30000;
  private static final int APPLICATION_NAME_LENGTH = 10;
  private static final String APPLICATION = "empty-mule-deploy-cloudhub-project";
  private static final String APPLICATION_NAME = randomAlphabetic(APPLICATION_NAME_LENGTH).toLowerCase();
  private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";
  private static final String DEPLOYMENT_TIMEOUT = "1000000";
  private static final String STARTED_STATUS = "STARTED";

  private static Stream<Arguments> muleVersions() {
    return Stream.of(
                     Arguments.of("4.4.0-SNAPSHOT"));
  }

  private Verifier verifier;
  private CloudHubClient cloudHubClient;

  @Override
  public void testSuccessful(ExtensionContext context) {
    cloudHubClient.deleteApplications(APPLICATION_NAME);
  }

  public String getApplication() {
    return APPLICATION;
  }

  public void before(String muleVersion) throws VerificationException, IOException {
    log.info("Initializing context...");

    verifier = buildBaseVerifier();
    verifier.setEnvironmentVariable("username", username);
    verifier.setEnvironmentVariable("password", password);
    verifier.setEnvironmentVariable("environment", PRODUCTION_ENVIRONMENT);
    verifier.setEnvironmentVariable("mule.version", muleVersion);
    verifier.setEnvironmentVariable("cloudhub.application.name", APPLICATION_NAME);
    verifier.setEnvironmentVariable("cloudhub.deployment.timeout", DEPLOYMENT_TIMEOUT);
  }

  @ParameterizedTest
  @MethodSource("muleVersions")
  public void testCloudHubDeploy(String muleVersion)
      throws VerificationException, InterruptedException, TimeoutException, IOException {
    before(muleVersion);
    cloudHubClient = new CloudHubClient(getCloudhubDeployment(), null);
    String version = muleVersion.replace(SNAPSHOT_SUFFIX, "");

    assertThat(cloudHubClient.getSupportedMuleVersions().stream()
        .map(org.mule.tools.client.cloudhub.model.SupportedVersion::getVersion)
        .collect(Collectors.toSet()).contains(version)).describedAs("Version not supported by CloudHub");

    log.info("Executing mule:deploy goal...");
    verifier.addCliOption("-DmuleDeploy");

    verifier.executeGoal(DEPLOY_GOAL);

    String status = validateApplicationIsInStatus(APPLICATION_NAME, STARTED_STATUS);
    assertThat(status).isEqualTo(STARTED_STATUS).describedAs("Application was not deployed");

    verifier.verifyErrorFreeLog();
  }

  @ParameterizedTest
  @MethodSource("muleVersions")
  public void testCloudHubDeployWithInvalidOrg(String muleVersion) throws VerificationException, IOException {
    before(muleVersion);
    CloudHubDeployment cloudHubDeployment = getCloudhubDeployment();
    cloudHubDeployment.setBusinessGroupId("notValidOrg");
    cloudHubClient = new CloudHubClient(cloudHubDeployment, null);

    Assertions.assertThrows(
                            VerificationException.class,
                            () -> {
                              log.info("Executing mule:deploy goal...");
                              verifier.addCliOption("-DmuleDeploy");
                              verifier.executeGoal(DEPLOY_GOAL);
                            },
                            "Cannot get the environment, the business group is not valid");

  }

  private CloudHubDeployment getCloudhubDeployment() {
    CloudHubDeployment cloudHubDeployment = new CloudHubDeployment();
    cloudHubDeployment.setUsername(username);
    cloudHubDeployment.setPassword(password);
    cloudHubDeployment.setUri(DEFAULT_BASE_URL);
    cloudHubDeployment.setEnvironment(PRODUCTION_ENVIRONMENT);
    return cloudHubDeployment;
  }

  private String validateApplicationIsInStatus(String applicationName, String status)
      throws TimeoutException, InterruptedException {
    log.debug("Checking application " + applicationName + " for status " + status + "...");

    ApplicationStatusRetriableOperation operation = new ApplicationStatusRetriableOperation(status, applicationName);

    OperationRetrier operationRetrier = new OperationRetrier();
    operationRetrier.setSleepTime(RETRY_SLEEP_TIME);

    operationRetrier.retry(operation);

    return operation.getApplicationStatus();

  }

  class ApplicationStatusRetriableOperation implements RetriableOperation {

    private String applicationStatus;
    private final String expectedStatus;
    private final String applicationName;

    public ApplicationStatusRetriableOperation(String expectedStatus, String applicationName) {
      this.expectedStatus = expectedStatus;
      this.applicationName = applicationName;
    }

    public String getApplicationStatus() {
      return applicationStatus;
    }

    @Override
    public Boolean run() {
      Application application = cloudHubClient.getApplications(applicationName);
      return application == null || !expectedStatus.equals(application.getStatus());
    }
  }
}
