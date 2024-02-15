/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.deployment.cloudhub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mule.tools.client.arm.model.User;
import org.mule.tools.client.arm.model.UserInfo;
import org.mule.tools.client.cloudhub.model.Application;
import org.mule.tools.client.cloudhub.CloudHubClient;
import org.mule.tools.client.cloudhub.model.Environment;
import org.mule.tools.client.cloudhub.model.LatestUpdate;
import org.mule.tools.client.cloudhub.model.SupportedVersion;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.anypoint.CloudHubDeployment;
import org.mule.tools.utils.DeployerLog;
import org.mule.tools.verification.cloudhub.CloudHubDeploymentVerification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CloudHubArtifactDeployerTest {

  private static final String FAKE_APPLICATION_NAME = "fake-name";

  @TempDir
  Path temporaryFolder;

  private File applicationFile;
  private UserInfo userInfo;
  private DeployerLog logMock;
  private CloudHubClient clientMock;
  private Application applicationMock;
  private CloudHubDeployment deploymentMock;
  private CloudHubArtifactDeployer cloudHubArtifactDeployerSpy;

  private CloudHubArtifactDeployer cloudHubArtifactDeployer;

  @BeforeEach
  public void setUp() throws IOException {
    applicationFile = temporaryFolder.toFile();
    userInfo = createUserInfo();

    logMock = mock(DeployerLog.class);
    clientMock = mock(CloudHubClient.class);
    applicationMock = mock(Application.class);
    deploymentMock = mock(CloudHubDeployment.class);

    when(deploymentMock.getApplicationName()).thenReturn(FAKE_APPLICATION_NAME);
    when(deploymentMock.getMuleVersion()).thenReturn(Optional.of("4.0.0"));
    when(deploymentMock.getArtifact()).thenReturn(applicationFile);
    when(deploymentMock.getWorkers()).thenReturn(1);
    when(deploymentMock.getWorkerType()).thenReturn("Micro");


    when(clientMock.getApplications(FAKE_APPLICATION_NAME)).thenReturn(applicationMock);
    when(clientMock.getMe()).thenReturn(userInfo);

    cloudHubArtifactDeployer = new CloudHubArtifactDeployer(deploymentMock, clientMock, logMock);
    cloudHubArtifactDeployerSpy = spy(cloudHubArtifactDeployer);

  }

  @Test
  public void deployDomainTest() {
    assertThatThrownBy(() -> cloudHubArtifactDeployer.deployDomain()).isExactlyInstanceOf(DeploymentException.class);

  }

  @Test
  public void undeployDomainTest() {
    assertThatThrownBy(() -> cloudHubArtifactDeployer.undeployDomain()).isExactlyInstanceOf(DeploymentException.class);
  }

  @Test
  public void deployApplicationNew() throws DeploymentException {
    when(clientMock.isDomainAvailable(any())).thenReturn(true);
    when(deploymentMock.getDisableCloudHubLogs()).thenReturn(Boolean.TRUE);

    doNothing().when(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();

    cloudHubArtifactDeployerSpy.deployApplication();

    verify(clientMock).isDomainAvailable(any());
    verify(cloudHubArtifactDeployerSpy).createOrUpdateApplication();
    ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
    verify(clientMock).createApplication(applicationCaptor.capture(), any());
    assertThat(applicationCaptor.getValue().getLoggingCustomLog4JEnabled()).describedAs("Application has logging disable")
        .isTrue();
    verify(cloudHubArtifactDeployerSpy).startApplication();
    verify(clientMock).startApplications(FAKE_APPLICATION_NAME);
    verify(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();
  }

  @Test
  public void deployApplicationNew_ForClient_adds_userid() throws DeploymentException {
    UserInfo clientUserInfo = createUserInfo();
    clientUserInfo.user.isClient = true;
    when(clientMock.getMe()).thenReturn(clientUserInfo);
    when(clientMock.isDomainAvailable(any())).thenReturn(true);
    doNothing().when(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();
    cloudHubArtifactDeployerSpy.deployApplication();
    ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
    verify(clientMock).createApplication(applicationCaptor.capture(), any());
    assertThat(clientUserInfo.user.id.equalsIgnoreCase(applicationCaptor.getValue().getUserId()))
        .describedAs("application does not have userid").isTrue();
    verify(clientMock).isDomainAvailable(any());
    verify(cloudHubArtifactDeployerSpy).createOrUpdateApplication();
    verify(cloudHubArtifactDeployerSpy).createApplication();
    verify(cloudHubArtifactDeployerSpy).startApplication();
    verify(clientMock).startApplications(FAKE_APPLICATION_NAME);
    verify(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();
  }

  @Test
  public void deployApplicationNew_ForUser_does_not_userid() throws DeploymentException {
    UserInfo clientUserInfo = createUserInfo();
    clientUserInfo.user.isClient = false;
    when(clientMock.getMe()).thenReturn(clientUserInfo);
    when(clientMock.isDomainAvailable(any())).thenReturn(true);
    doNothing().when(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();
    cloudHubArtifactDeployerSpy.deployApplication();
    ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
    verify(clientMock).createApplication(applicationCaptor.capture(), any());
    assertThat(applicationCaptor.getValue().getUserId()).describedAs("application has userid").isNull();
    verify(clientMock).isDomainAvailable(any());
    verify(cloudHubArtifactDeployerSpy).createOrUpdateApplication();
    verify(cloudHubArtifactDeployerSpy).createApplication();
    verify(cloudHubArtifactDeployerSpy).startApplication();
    verify(clientMock).startApplications(FAKE_APPLICATION_NAME);
    verify(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();
  }

  @Test
  public void deployApplicationSkipVerification() throws DeploymentException {
    when(clientMock.isDomainAvailable(any())).thenReturn(true);

    when(deploymentMock.getSkipDeploymentVerification()).thenReturn(true);

    cloudHubArtifactDeployerSpy.deployApplication();


    verify(clientMock).isDomainAvailable(any());
    verify(cloudHubArtifactDeployerSpy).createOrUpdateApplication();
    verify(cloudHubArtifactDeployerSpy).createApplication();
    verify(cloudHubArtifactDeployerSpy).startApplication();
    verify(clientMock).startApplications(FAKE_APPLICATION_NAME);
    verify(cloudHubArtifactDeployerSpy, never()).checkApplicationHasStarted();
  }

  private UserInfo createUserInfo() {
    UserInfo userInfo = new UserInfo();
    User user = new User();
    user.isClient = false;
    user.id = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    userInfo.user = user;
    return userInfo;
  }

  @Test
  public void deployApplicationUpdate() throws DeploymentException {
    when(clientMock.isDomainAvailable(any())).thenReturn(false);

    doNothing().when(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();

    cloudHubArtifactDeployerSpy.deployApplication();

    verify(clientMock).isDomainAvailable(any());
    verify(cloudHubArtifactDeployerSpy).createOrUpdateApplication();
    verify(cloudHubArtifactDeployerSpy).updateApplication();
    verify(cloudHubArtifactDeployerSpy).startApplication();
    verify(clientMock).startApplications(FAKE_APPLICATION_NAME);
    verify(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();
  }

  @Test
  public void deployApplicationUpdateWhenLoggingCustomLog4JEnabledIsPresentShouldBeSet() throws DeploymentException {
    when(clientMock.isDomainAvailable(any())).thenReturn(false);
    when(deploymentMock.getDisableCloudHubLogs()).thenReturn(Boolean.TRUE);
    when(applicationMock.getLoggingCustomLog4JEnabled()).thenReturn(Boolean.FALSE);

    doNothing().when(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();

    cloudHubArtifactDeployerSpy.deployApplication();

    ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
    verify(clientMock).updateApplication(applicationCaptor.capture(), any());
    assertThat(applicationCaptor.getValue().getLoggingCustomLog4JEnabled()).describedAs("Application has logging disable")
        .isTrue();
  }

  @Test
  public void deployApplicationUpdateWhenLoggingCustomLog4JEnabledIsNullInPomTheValueShouldBeSetTheOriginalApp()
      throws DeploymentException {
    when(clientMock.isDomainAvailable(any())).thenReturn(false);
    when(deploymentMock.getDisableCloudHubLogs()).thenReturn(null);
    when(applicationMock.getLoggingCustomLog4JEnabled()).thenReturn(Boolean.TRUE);

    doNothing().when(cloudHubArtifactDeployerSpy).checkApplicationHasStarted();

    cloudHubArtifactDeployerSpy.deployApplication();

    ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
    verify(clientMock).updateApplication(applicationCaptor.capture(), any());
    assertThat(applicationCaptor.getValue().getLoggingCustomLog4JEnabled()).describedAs("Application has logging disable")
        .isTrue();
  }

  @Test
  public void deployApplicationVerificationStartedFail() throws DeploymentException {
    CloudHubDeploymentVerification verificationMock = mock(CloudHubDeploymentVerification.class);
    doThrow(DeploymentException.class).when(verificationMock).assertDeployment(deploymentMock);
    when(clientMock.isDomainAvailable(any())).thenReturn(true);

    assertThatThrownBy(() -> {
      cloudHubArtifactDeployer.setDeploymentVerification(verificationMock);
      cloudHubArtifactDeployer.deployApplication();
    }).isExactlyInstanceOf(DeploymentException.class);

    verify(clientMock).isDomainAvailable(any());
    verify(verificationMock).assertDeployment(eq(deploymentMock));
  }

  @Test
  public void undeployApplicationTest() throws DeploymentException {
    cloudHubArtifactDeployer.undeployApplication();
    verify(clientMock, times(1)).stopApplications(FAKE_APPLICATION_NAME);
    verify(clientMock, times(1)).deleteApplications(FAKE_APPLICATION_NAME);
  }

  @Test
  public void getApplicationNameTest() {
    assertThat(cloudHubArtifactDeployer.getApplicationName()).describedAs("Application name is not the expected")
        .isEqualTo(FAKE_APPLICATION_NAME);
  }

  private Application createApplication(String name) {
    Application app = new Application();
    app.setDomain("app" + name);
    return app;
  }

  @Test
  public void resolvePropertiesNotSetAndOverrideTrue() {
    Map<String, String> originalProperties = new HashMap<>();
    originalProperties.put("foo", "bar");
    Map<String, String> resolvedProperties = cloudHubArtifactDeployer.resolveProperties(originalProperties, null, true);
    assertThat(resolvedProperties.size()).describedAs("originalProperties should have the same size").isEqualTo(1);
    assertThat(resolvedProperties).describedAs("resolvedProperties should contains the (foo,bar) entry").containsEntry("foo",
                                                                                                                       "bar");
  }

  @Test
  public void resolvePropertiesNotSetAndOverrideFalse() {
    Map<String, String> originalProperties = new HashMap<>();
    originalProperties.put("foo", "bar");
    Map<String, String> resolvedProperties = cloudHubArtifactDeployer.resolveProperties(originalProperties, null, false);
    assertThat(resolvedProperties.size()).describedAs("originalProperties should have the same size").isEqualTo(1);
    assertThat(resolvedProperties).describedAs("resolvedProperties should contains the (foo,bar) entry").containsEntry("foo",
                                                                                                                       "bar");
  }

  @Test
  public void resolvePropertiesSetAndOverrideTrue() {
    Map<String, String> originalProperties = new HashMap<>();
    originalProperties.put("foo", "bar");
    Map<String, String> properties = new HashMap<>();
    properties.put("key", "val");
    properties.put("foo", "lala");
    Map<String, String> resolvedProperties = cloudHubArtifactDeployer.resolveProperties(originalProperties, properties, true);
    assertThat(resolvedProperties.size()).describedAs("resolvedProperties does not have the expected size").isEqualTo(2);
    assertThat(resolvedProperties).describedAs("resolvedProperties should contains the (key,val) entry").containsEntry("key",
                                                                                                                       "val");
    assertThat(resolvedProperties).describedAs("resolvedProperties should contains the (foo,lala) entry").containsEntry("foo",
                                                                                                                        "lala");
  }

  @Test
  public void resolvePropertiesEmptyAndOverride() {
    Map<String, String> originalProperties = new HashMap<>();
    originalProperties.put("foo", "bar");
    Map<String, String> properties = new HashMap<>();
    Map<String, String> resolvedProperties = cloudHubArtifactDeployer.resolveProperties(originalProperties, properties, true);
    assertThat(resolvedProperties.size()).describedAs("resolvedProperties does not have the expected size").isEqualTo(0);
  }

  @Test
  public void resolvePropertiesSetAndOverrideFalse() {
    Map<String, String> originalProperties = new HashMap<>();
    originalProperties.put("foo", "bar");
    Map<String, String> properties = new HashMap<>();
    properties.put("key", "val");
    properties.put("foo", "lala");
    Map<String, String> resolvedProperties = cloudHubArtifactDeployer.resolveProperties(originalProperties, properties, false);
    assertThat(resolvedProperties.size()).describedAs("resolvedProperties does not have the expected size").isEqualTo(2);
    assertThat(resolvedProperties).describedAs("resolvedProperties should contains the (key,val) entry").containsEntry("key",
                                                                                                                       "val");
    assertThat(resolvedProperties).describedAs("resolvedProperties should contains the (foo,bar) entry").containsEntry("foo",
                                                                                                                       "bar");
  }

  @Test
  public void testDeployApplicationObjectStoreV1() throws DeploymentException {
    when(deploymentMock.getObjectStoreV2()).thenReturn(false);
    when(deploymentMock.getSkipDeploymentVerification()).thenReturn(true);

    when(clientMock.isDomainAvailable(any())).thenReturn(true);

    cloudHubArtifactDeployer.deployApplication();

    ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
    verify(clientMock).createApplication(applicationCaptor.capture(), any());
    assertThat(applicationCaptor.getValue().getObjectStoreV1()).describedAs("ObjectStoreV1 must be true").isTrue();
  }

  @Test
  public void testDeployApplicationObjectStoreV2() throws DeploymentException {
    when(deploymentMock.getObjectStoreV2()).thenReturn(true);
    when(deploymentMock.getSkipDeploymentVerification()).thenReturn(true);

    when(clientMock.isDomainAvailable(any())).thenReturn(true);

    cloudHubArtifactDeployer.deployApplication();

    ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
    verify(clientMock).createApplication(applicationCaptor.capture(), any());
    assertThat(applicationCaptor.getValue().getObjectStoreV1()).describedAs("ObjectStoreV1 must be false").isFalse();
  }

  @Disabled("V1 is no longer used: https://docs.mulesoft.com/object-store/osv2-faq")
  @Test
  public void testObjectStorageV1FromEnvironment() throws DeploymentException {
    CloudHubDeployment deployment = new CloudHubDeployment();

    deployment.setSkipDeploymentVerification(true);
    deployment.setApplicationName(FAKE_APPLICATION_NAME);
    deployment.setMuleVersion("4.0.0");
    deployment.setArtifact(applicationFile);
    deployment.setWorkers(1);
    deployment.setWorkerType("Micro");

    cloudHubArtifactDeployer = new CloudHubArtifactDeployer(deployment, clientMock, logMock);

    when(clientMock.isDomainAvailable(any())).thenReturn(true);

    Environment mockEnvironment = mock(Environment.class);
    when(clientMock.getSupportedMuleVersions()).thenReturn(getObjectStoreV1Enabled(true, "4.0.0"));

    when(clientMock.getEnvironment()).thenReturn(mockEnvironment);

    cloudHubArtifactDeployer.deployApplication();

    ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
    verify(clientMock).createApplication(applicationCaptor.capture(), any());
    assertThat(applicationCaptor.getValue().getObjectStoreV1()).describedAs("ObjectStoreV1 must be true").isTrue();
  }

  @Test
  public void testObjectStorageV2FromEnvironment() throws DeploymentException {
    CloudHubDeployment deployment = new CloudHubDeployment();

    deployment.setSkipDeploymentVerification(true);
    deployment.setApplicationName(FAKE_APPLICATION_NAME);
    deployment.setMuleVersion("4.0.0");
    deployment.setArtifact(applicationFile);
    deployment.setWorkers(1);
    deployment.setWorkerType("Micro");

    cloudHubArtifactDeployer = new CloudHubArtifactDeployer(deployment, clientMock, logMock);

    when(clientMock.isDomainAvailable(any())).thenReturn(true);

    Environment mockEnvironment = mock(Environment.class);

    when(clientMock.getEnvironment()).thenReturn(mockEnvironment);
    when(clientMock.getSupportedMuleVersions()).thenReturn(getObjectStoreV1Enabled(false, "4.0.0"));

    cloudHubArtifactDeployer.deployApplication();

    ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
    verify(clientMock).createApplication(applicationCaptor.capture(), any());
    assertThat(applicationCaptor.getValue().getObjectStoreV1()).describedAs("ObjectStoreV1 must be true").isFalse();
  }

  private static Stream<Arguments> argumentsForDisableCloudHubLogs() {
    return Stream.of(
                     Arguments.of(null, null, false),
                     Arguments.of(false, null, false),
                     Arguments.of(true, null, true),
                     Arguments.of(null, false, false),
                     Arguments.of(false, false, false),
                     Arguments.of(true, false, true),
                     Arguments.of(null, true, true),
                     Arguments.of(false, true, false),
                     Arguments.of(true, true, true));
  }

  @ParameterizedTest
  @MethodSource("argumentsForDisableCloudHubLogs")
  public void testDisableCloudHubLogs(Boolean configured, Boolean current, Boolean expected) throws DeploymentException {
    CloudHubDeployment deployment = new CloudHubDeployment();
    deployment.setSkipDeploymentVerification(true);
    deployment.setApplicationName(FAKE_APPLICATION_NAME);
    deployment.setMuleVersion("4.0.0");
    deployment.setArtifact(applicationFile);
    deployment.setWorkers(1);
    deployment.setWorkerType("Micro");
    deployment.setDisableCloudHubLogs(configured);

    Application application = new Application();
    application.setLoggingCustomLog4JEnabled(current);

    cloudHubArtifactDeployer = new CloudHubArtifactDeployer(deployment, clientMock, logMock);

    when(clientMock.getApplications(anyString())).thenReturn(application);

    cloudHubArtifactDeployer.deployApplication();

    ArgumentCaptor<Application> applicationCaptor = ArgumentCaptor.forClass(Application.class);
    verify(clientMock).updateApplication(applicationCaptor.capture(), any());

    assertThat(applicationCaptor.getValue().getLoggingCustomLog4JEnabled()).describedAs("DisableCloudHubLogs must be " + expected)
        .isEqualTo(expected);
  }

  private List<SupportedVersion> getObjectStoreV1Enabled(boolean enabled, String muleVersion) {
    List<SupportedVersion> supportedVersions = new ArrayList<SupportedVersion>();
    SupportedVersion version = new SupportedVersion();
    LatestUpdate update = new LatestUpdate();
    HashMap<String, Boolean> flags = new HashMap<String, Boolean>();
    flags.put(CloudHubArtifactDeployer.OBJECT_STOREV1, enabled);
    update.setFlags(flags);
    version.setLatestUpdate(update);
    version.setVersion(muleVersion);
    supportedVersions.add(version);
    return supportedVersions;
  }
}
