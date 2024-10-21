/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import org.apache.maven.shared.verifier.VerificationException;
import org.apache.maven.shared.verifier.Verifier;
import org.junit.jupiter.api.Test;
import org.mule.tools.client.OperationRetrier;
import org.mule.tools.client.OperationRetrier.RetriableOperation;
import org.mule.tools.client.fabric.model.ApplicationDetailResponse;
import org.mule.tools.deployment.cloudhub2.Cloudhub2RuntimeFabricClient;
import org.mule.tools.model.anypoint.Cloudhub2Deployment;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mule.tools.client.AbstractMuleClient.DEFAULT_BASE_URL;

public class Cloudhub2UBPDeploymentTest extends AbstractDeploymentTest {

  private static final int APPLICATION_NAME_LENGTH = 10;
  private static final String EXPECTED_STATUS = "RUNNING";
  private static final long RETRY_SLEEP_TIME = 60000;
  private static final String TARGET = "Cloudhub-US-East-2";
  private static final String SANDBOX_ENVIRONMENT = "Sandbox";
  private static final String SERVER = "anypoint-exchange-v3";
  private static final String PROJECT_GROUP_ID = "9cd3ec6f-e4d1-45a4-a83a-769586f8618d";
  private static final String PROVIDER = "MC";
  private static final String INSTANCETYPE = "mule.micro";
  private static final Boolean PERSISTENT_OBJECT_STORE = true;
  private static final Boolean TRACING_ENABLED = false;
  private static final Boolean A_ENABLED = true;
  private static final Integer MIN_REPLICAS = 1;
  private static final Integer MAX_REPLICAS = 2;

  private Verifier verifier;
  private String application;
  private String applicationName;

  public String getApplication() {
    return application;
  }

  public String getApplicationName() {
    return applicationName;
  }

  public void before(String muleVersion, String application) throws Exception {
    LOG.info("Initializing context...");
    this.application = application;
    this.applicationName = randomAlphabetic(APPLICATION_NAME_LENGTH).toLowerCase();
    verifier = buildBaseVerifier();
    verifier.setSystemProperty("target", TARGET);
    verifier.setSystemProperty("mule.version", muleVersion);
    verifier.setSystemProperty("project.groupId", PROJECT_GROUP_ID);
    verifier.setSystemProperty("cloudhub2.application.name", applicationName);
    verifier.setSystemProperty("username", getUsernameUBP());
    verifier.setSystemProperty("password", getPasswordUBP());
    verifier.setSystemProperty("provider", PROVIDER);
    verifier.setSystemProperty("environment", SANDBOX_ENVIRONMENT);
    verifier.setSystemProperty("instanceType", INSTANCETYPE);
    verifier.setSystemProperty("persistentObjectStore", String.valueOf(PERSISTENT_OBJECT_STORE));
    verifier.setSystemProperty("tracingEnabled", String.valueOf(TRACING_ENABLED));
    verifier.setSystemProperty("autoscalingEnabled", String.valueOf(A_ENABLED));
    verifier.setSystemProperty("minReplicas", String.valueOf(MIN_REPLICAS));
    verifier.setSystemProperty("maxReplicas", String.valueOf(MAX_REPLICAS));
  }

  @Test
  public void cloudhub2UBPDeployTest() throws Exception {
    before("4.7.1", "empty-mule-deploy-cloudhub2-UBP-project");
    LOG.info("Executing deploy to CH2 integration test with an valid UBP POM Project. It should deploy correctly");
    verifier.addCliArguments(DEPLOY_GOAL, "-DmuleDeploy");
    verifier.execute();
    Cloudhub2RuntimeFabricClient cloudhub2Client = new Cloudhub2RuntimeFabricClient(getCloudhub2Deployment(), null);

    String applicationId = cloudhub2Client.getDeployments().items.stream()
        .filter(deployment -> applicationName.equals(deployment.name))
        .map(deployment -> deployment.id)
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Application not found"));

    String status = validateApplicationIsInStatus(cloudhub2Client, getApplicationName(), applicationId);
    assertThat(status).describedAs("Application was not deployed").isEqualTo(EXPECTED_STATUS);
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testCloudhub2UBPDeployWithInvalidOrg() throws Exception {
    assertThatThrownBy(() -> {
      before("4.7.1", "empty-mule-deploy-cloudhub2-UBP-invalid-group-project");
      LOG.debug("Executing deploy to CH2 integration test with an Invalid UBP POM Project. It should not deploy");
      verifier.addCliArguments(DEPLOY_GOAL, "-DmuleDeploy");
      verifier.execute();
    }).isExactlyInstanceOf(VerificationException.class)
        .hasMessageContaining("java.lang.IllegalStateException: Cannot get the environment, the business group is not valid");
  }

  private Cloudhub2Deployment getCloudhub2Deployment() {
    Cloudhub2Deployment cloudhub2Deployment = new Cloudhub2Deployment();
    cloudhub2Deployment.setUsername(getUsername());
    cloudhub2Deployment.setPassword(getPassword());
    cloudhub2Deployment.setEnvironment(SANDBOX_ENVIRONMENT);
    cloudhub2Deployment.setUri(DEFAULT_BASE_URL);
    cloudhub2Deployment.setApplicationName(getApplicationName());
    return cloudhub2Deployment;
  }

  private String validateApplicationIsInStatus(Cloudhub2RuntimeFabricClient cloudhub2Client, String applicationName,
                                               String applicationId)
      throws Exception {
    LOG.debug("Checking application {} for status as " + EXPECTED_STATUS + "...", applicationName);

    ApplicationStatusRetriableOperation operation =
        new ApplicationStatusRetriableOperation(cloudhub2Client, applicationId);

    OperationRetrier operationRetrier = new OperationRetrier();
    operationRetrier.setSleepTime(RETRY_SLEEP_TIME);

    operationRetrier.retry(operation);

    return operation.getApplicationStatus();
  }

  private static class ApplicationStatusRetriableOperation implements RetriableOperation {

    private String applicationStatus;

    private final String applicationId;
    private final Cloudhub2RuntimeFabricClient cloudhub2Client;

    public ApplicationStatusRetriableOperation(Cloudhub2RuntimeFabricClient cloudhub2Client,
                                               String applicationId) {
      this.applicationId = applicationId;
      this.cloudhub2Client = cloudhub2Client;
    }

    public String getApplicationStatus() {
      return applicationStatus;
    }

    @Override
    public Boolean run() {
      ApplicationDetailResponse application = cloudhub2Client.getDeployment(applicationId).application;
      if (application != null) {
        applicationStatus = cloudhub2Client.getDeployment(applicationId).application.status;
        return !EXPECTED_STATUS.equals(applicationStatus);
      }
      return true;
    }
  }
}
