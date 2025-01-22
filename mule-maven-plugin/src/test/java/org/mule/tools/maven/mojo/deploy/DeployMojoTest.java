/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.deploy;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mule.tools.api.util.MavenProjectInformation;
import org.mule.tools.api.util.Project;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.deployment.Deployer;
import org.mule.tools.deployment.agent.AgentDomainDeployer;
import org.mule.tools.deployment.arm.ArmApplicationDeployer;
import org.mule.tools.deployment.standalone.StandaloneApplicationDeployer;
import org.mule.tools.maven.config.proxy.ProxyConfiguration;
import org.mule.tools.model.Deployment;
import org.mule.tools.model.agent.AgentDeployment;
import org.mule.tools.model.anypoint.ArmDeployment;
import org.mule.tools.model.anypoint.MavenResolverMetadata;
import org.mule.tools.model.standalone.StandaloneDeployment;
import org.mule.tools.validation.AbstractDeploymentValidator;
import org.mule.tools.validation.DeploymentValidatorFactory;
import org.mule.tools.validation.EnvironmentSupportedVersions;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_POLICY;
import static org.mule.tools.deployment.AbstractDeployerFactory.MULE_APPLICATION_CLASSIFIER;
import static org.mule.tools.deployment.AbstractDeployerFactory.MULE_DOMAIN_CLASSIFIER;
import static org.mule.tools.maven.config.proxy.ProxyConfiguration.HTTP_PROXY_HOST;

class DeployMojoTest {

  static class DeployMojoImpl extends DeployMojo {

    private MavenResolverMetadata mavenResolver;

    @Override
    public MavenResolverMetadata getMetadata() {
      return mavenResolver;
    }

    public void setMavenResolver(MavenResolverMetadata mavenResolver) {
      this.mavenResolver = mavenResolver;
    }
  }

  static class CustomDeploymentValidator extends AbstractDeploymentValidator {

    private boolean throwException = false;

    public CustomDeploymentValidator() {
      super(null);
    }

    @Override
    public void validateMuleVersionAgainstEnvironment() throws DeploymentException {
      if (throwException) {
        throw new DeploymentException("An exception was thrown");
      }
    }

    @Override
    public EnvironmentSupportedVersions getEnvironmentSupportedVersions() throws DeploymentException {
      return null;
    }

    public boolean isThrowException() {
      return throwException;
    }

    public void setThrowException(boolean throwException) {
      this.throwException = throwException;
    }
  }

  private static MockedStatic<MavenProjectInformation> MAVEN_PROJECT_INFORMATION;
  private static MockedStatic<DeploymentValidatorFactory> DEPLOYMENT_VALIDATOR_FACTORY;
  private static final MavenProjectInformation PROJECT_INFORMATION = mock(MavenProjectInformation.class);
  private static final Project PROJECT = mock(Project.class);
  private static final CustomDeploymentValidator DEPLOYMENT_VALIDATOR = new CustomDeploymentValidator();

  @TempDir
  public File projectBaseFolder;
  @Mock
  private Settings settings;
  @Mock
  private MavenProject project;
  @Mock
  private Deployment deployment;
  @Mock
  private MavenSession session;
  @Mock
  private MavenResolverMetadata mavenResolverMetadata;
  @Mock
  private ArtifactFactory artifactFactory;
  @Mock
  private ArtifactResolver artifactResolver;
  @Mock
  private Artifact artifact;
  @InjectMocks
  private DeployMojoImpl mojo = new DeployMojoImpl();
  private AutoCloseable autoCloseable;

  @SuppressWarnings("unchecked")
  @BeforeAll
  static void staticSetup() {
    MAVEN_PROJECT_INFORMATION = mockStatic(MavenProjectInformation.class);
    DEPLOYMENT_VALIDATOR_FACTORY = mockStatic(DeploymentValidatorFactory.class);

    DEPLOYMENT_VALIDATOR_FACTORY.when(() -> DeploymentValidatorFactory.createDeploymentValidator(nullable(Deployment.class)))
        .thenReturn(DEPLOYMENT_VALIDATOR);
    MAVEN_PROJECT_INFORMATION
        .when(() -> MavenProjectInformation
            .getProjectInformation(nullable(MavenSession.class), nullable(MavenProject.class), nullable(File.class),
                                   nullable(Boolean.class), nullable(List.class), nullable(String.class), nullable(List.class)))
        .thenReturn(PROJECT_INFORMATION);
  }

  @AfterAll
  static void staticTearDown() {
    MAVEN_PROJECT_INFORMATION.close();
    DEPLOYMENT_VALIDATOR_FACTORY.close();
  }

  @BeforeEach
  void setUp() throws Exception {
    autoCloseable = MockitoAnnotations.openMocks(this);
    // WHEN
    setProject();
    System.setProperty("muleDeploy", "true");
    when(session.getGoals()).thenReturn(Collections.singletonList("deploy"));
    when(deployment.validateVersion()).thenReturn(false);
    when(mavenResolverMetadata.getProject()).thenReturn(project);
    when(mavenResolverMetadata.getFactory()).thenReturn(artifactFactory);
    when(mavenResolverMetadata.getResolver()).thenReturn(artifactResolver);
    when(artifact.getFile()).thenReturn(projectBaseFolder);
    when(artifactFactory.createArtifactWithClassifier(nullable(String.class), nullable(String.class), nullable(String.class),
                                                      nullable(String.class), nullable(String.class))).thenReturn(artifact);
    when(PROJECT_INFORMATION.getProject()).thenReturn(PROJECT);
    when(PROJECT_INFORMATION.getProjectBaseFolder()).thenReturn(projectBaseFolder.toPath());
    when(PROJECT_INFORMATION.getBuildDirectory()).thenReturn(projectBaseFolder.toPath());
    when(settings.getProxies()).thenReturn(Collections.singletonList(new Proxy()));
    //
    DEPLOYMENT_VALIDATOR.setThrowException(false);
    mojo.setProjectBaseFolder(projectBaseFolder);
  }

  @AfterEach
  void tearDown() throws Exception {
    autoCloseable.close();
  }

  @ParameterizedTest
  @MethodSource("executeTestValue")
  void executeTest(Class<? extends Deployer> clazz, Deployment deployment, String packaging, Class<? extends Throwable> exception,
                   String message, DeploymentException deploymentException, boolean withException, Boolean skip,
                   int validationError)
      throws MojoExecutionException, MojoFailureException {
    deployment.setPackaging(packaging);
    deployment.setSkip(Optional.ofNullable(skip).map(String::valueOf).orElse(""));
    DEPLOYMENT_VALIDATOR.setThrowException(withException);
    when(PROJECT_INFORMATION.getDeployments()).thenReturn(Collections.singletonList(deployment));
    System.clearProperty(HTTP_PROXY_HOST);
    switch (validationError) {
      case 0:
        when(PROJECT_INFORMATION.isDeployment()).thenReturn(true);
        when(PROJECT_INFORMATION.getClassifier()).thenReturn(MULE_POLICY.toString());
        break;
      case 1:
        when(PROJECT_INFORMATION.getDeployments()).thenReturn(null);
        break;
      case 2:
        when(PROJECT_INFORMATION.getDeployments()).thenReturn(Collections.emptyList());
        break;
      case 3:
        when(PROJECT_INFORMATION.getDeployments()).thenReturn(Arrays.asList(deployment, deployment));
        break;
      case 4:
      case 5:
        System.setProperty(HTTP_PROXY_HOST, "localhost");
        break;
    }
    //
    try (MockedConstruction<? extends Deployer> constructor = mockConstruction(clazz, (mock, context) -> {
      if (deploymentException != null) {
        doThrow(deploymentException).when(mock).deploy();
      } else {
        doNothing().when(mock).deploy();
      }
    }); MockedConstruction<ProxyConfiguration> mockConstructor = mockConstruction(ProxyConfiguration.class, (mock, context) -> {
      if (validationError == 4) {
        doThrow(new Exception()).when(mock).handleProxySettings();
      } else {
        doNothing().when(mock).handleProxySettings();
      }
    })) {
      if (exception != null) {
        assertThatThrownBy(mojo::execute).isInstanceOf(exception).hasMessageContaining(message);
      } else {
        mojo.execute();
      }
    }
  }

  static Stream<Arguments> executeTestValue() {
    String applicationName = UUID.randomUUID().toString();
    String path = UUID.randomUUID().toString();
    File artifact = mock(File.class);
    when(artifact.getPath()).thenReturn(path);
    AgentDeployment agentDeployment = new AgentDeployment() {

      @Override
      public void setDefaultValues(MavenProject project) throws DeploymentException {
        // DO NOTHING
      }
    };
    ArmDeployment armDeployment = new ArmDeployment() {

      @Override
      public void setDefaultValues(MavenProject project) throws DeploymentException {
        // DO NOTHING
      }
    };
    StandaloneDeployment standaloneDeployment = new StandaloneDeployment() {

      @Override
      public void setDefaultValues(MavenProject project) throws DeploymentException {
        // DO NOTHING
      }
    };

    armDeployment.setApplicationName(applicationName);
    armDeployment.setArtifact(artifact);
    agentDeployment.setApplicationName(applicationName);
    agentDeployment.setArtifact(artifact);
    standaloneDeployment.setApplicationName(applicationName);
    standaloneDeployment.setArtifact(artifact);

    return Stream.of(
                     Arguments.of(AgentDomainDeployer.class, agentDeployment, MULE_DOMAIN_CLASSIFIER, null, null, null, false,
                                  false, -1),
                     Arguments.of(ArmApplicationDeployer.class, armDeployment, MULE_APPLICATION_CLASSIFIER, null, null, null,
                                  false, false, -1),
                     Arguments.of(StandaloneApplicationDeployer.class, standaloneDeployment, MULE_APPLICATION_CLASSIFIER,
                                  null,
                                  null, null, false, null, -1),
                     Arguments.of(StandaloneApplicationDeployer.class, standaloneDeployment, MULE_APPLICATION_CLASSIFIER,
                                  null,
                                  null, null, false, true, -1),
                     Arguments.of(StandaloneApplicationDeployer.class, standaloneDeployment, MULE_APPLICATION_CLASSIFIER,
                                  null,
                                  null, null, false, false, 4),
                     Arguments.of(StandaloneApplicationDeployer.class, standaloneDeployment, MULE_APPLICATION_CLASSIFIER,
                                  null,
                                  null, null, false, false, 5),
                     Arguments.of(StandaloneApplicationDeployer.class, standaloneDeployment, MULE_APPLICATION_CLASSIFIER,
                                  MojoFailureException.class,
                                  "Failed to deploy [" + path + "]", null, true, false, -1),
                     Arguments.of(AgentDomainDeployer.class, agentDeployment, "Invalid", RuntimeException.class,
                                  "Deployment not supported: ", null, false, false, -1),
                     Arguments.of(AgentDomainDeployer.class, agentDeployment, MULE_DOMAIN_CLASSIFIER, MojoFailureException.class,
                                  "Failed to deploy [" + path + "]", new DeploymentException(""), false, false, -1),
                     Arguments.of(AgentDomainDeployer.class, agentDeployment, MULE_DOMAIN_CLASSIFIER, MojoFailureException.class,
                                  "Failed to deploy [" + path + "]", new DeploymentException(""), false, false, -1),
                     Arguments.of(AgentDomainDeployer.class, agentDeployment, MULE_DOMAIN_CLASSIFIER,
                                  MojoExecutionException.class,
                                  "Deployment configuration is not valid", null, false, false, 0),
                     Arguments.of(AgentDomainDeployer.class, agentDeployment, MULE_DOMAIN_CLASSIFIER,
                                  MojoExecutionException.class,
                                  "Deployment configuration is not valid", null, false, false, 1),
                     Arguments.of(AgentDomainDeployer.class, agentDeployment, MULE_DOMAIN_CLASSIFIER,
                                  MojoExecutionException.class,
                                  "Deployment configuration is not valid", null, false, false, 2),
                     Arguments.of(AgentDomainDeployer.class, agentDeployment, MULE_DOMAIN_CLASSIFIER,
                                  MojoExecutionException.class,
                                  "Deployment configuration is not valid", null, false, false, 3));
  }

  @Test
  void dummyTest() {
    assertThat("MULE_MAVEN_PLUGIN_DEPLOY_PREVIOUS_RUN_PLACEHOLDER").isEqualTo(mojo.getPreviousRunPlaceholder());
    assertThat(new DeployMojo().getMetadata()).isInstanceOf(MavenResolverMetadata.class);
  }

  private void setProject() {
    Build build = mock(Build.class);
    reset(project);

    when(build.getDirectory()).thenReturn(projectBaseFolder.getAbsolutePath());
    when(project.getPackaging()).thenReturn(MULE_APPLICATION_CLASSIFIER);
    when(project.getBasedir()).thenReturn(projectBaseFolder);
    when(project.getBuild()).thenReturn(build);
    when(project.getModel()).thenReturn(mock(Model.class));
    when(project.getGroupId()).thenReturn(UUID.randomUUID().toString());
    when(project.getArtifactId()).thenReturn(UUID.randomUUID().toString());
    when(project.getVersion()).thenReturn(UUID.randomUUID().toString());
    when(project.getDependencies()).thenReturn(Collections.emptyList());
  }
}
