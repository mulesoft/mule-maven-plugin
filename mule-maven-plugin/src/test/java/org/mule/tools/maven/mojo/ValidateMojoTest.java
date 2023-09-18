/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.validation.project.AbstractProjectValidator;

import java.util.Properties;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class ValidateMojoTest extends AbstractMuleMojoTest {

  private static final String MUNIT_RUNNER_ARTIFACT_ID = "munit-runner";
  private static final String MUNIT_TOOLS_ARTIFACT_ID = "munit-tools";
  private static final String MUNIT_GROUP_ID = "com.mulesoft.munit";

  private ValidateMojo mojoMock;
  private MavenExecutionRequest mavenExecutionRequestMock;

  private AbstractProjectValidator validatorMock;

  @BeforeEach
  public void before() {
    validatorMock = mock(AbstractProjectValidator.class);

    mojoMock = mock(ValidateMojo.class);
    mojoMock.project = projectMock;
    mojoMock.session = mavenSessionMock;
    mojoMock.validator = validatorMock;

    when(mojoMock.getLog()).thenReturn(logMock);

    mavenExecutionRequestMock = mock(MavenExecutionRequest.class);
  }

  @Test
  public void executeDoNotVerify() throws MojoFailureException, MojoExecutionException, ValidationException {
    mojoMock.skipValidation = true;

    doCallRealMethod().when(mojoMock).execute();
    mojoMock.execute();

    verify(mojoMock, times(0)).validateMavenEnvironment();
  }

  @Test
  public void execute() throws MojoFailureException, MojoExecutionException, ValidationException {
    when(validatorMock.isProjectValid(any())).thenReturn(true);
    when(mojoMock.getProjectValidator()).thenReturn(validatorMock);


    doCallRealMethod().when(mojoMock).execute();
    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.execute();

    verify(mojoMock, times(1)).validateMavenEnvironment();
  }

  @Test
  public void validateMavenEnvironmentValid() throws ValidationException {

    Properties systemProperties = new Properties();
    systemProperties.put("maven.version", "3.3.3");
    when(mavenExecutionRequestMock.getSystemProperties()).thenReturn(systemProperties);
    when(mavenSessionMock.getRequest()).thenReturn(mavenExecutionRequestMock);

    doCallRealMethod().when(mojoMock).validateMavenEnvironment();
    mojoMock.validateMavenEnvironment();
  }

  @Test
  public void validateMavenEnvironmentInvalid() throws ValidationException {

    Properties systemProperties = new Properties();
    systemProperties.put("maven.version", "3.3.2");
    when(mavenExecutionRequestMock.getSystemProperties()).thenReturn(systemProperties);
    when(mavenSessionMock.getRequest()).thenReturn(mavenExecutionRequestMock);

    doCallRealMethod().when(mojoMock).validateMavenEnvironment();
    assertThatThrownBy(() -> mojoMock.validateMavenEnvironment()).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void validateNotAllowedDependenciesValid() throws ValidationException {
    Dependency dependencyMock = mock(Dependency.class);

    when(dependencyMock.getGroupId()).thenReturn("fake.group.id");
    when(dependencyMock.getArtifactId()).thenReturn("fake-artifact-id");
    when(dependencyMock.getVersion()).thenReturn("0.0.0");
    when(dependencyMock.getScope()).thenReturn("provided");
    when(dependencyMock.getType()).thenReturn("mule-server-plugin");
    when(dependencyMock.getClassifier()).thenReturn("mule-server-plugin");

    when(projectMock.getDependencies()).thenReturn(singletonList(dependencyMock));
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);

    doCallRealMethod().when(mojoMock).buildArtifactCoordinates(any());
    doCallRealMethod().when(mojoMock).validateNotAllowedDependencies();
    mojoMock.validateNotAllowedDependencies();
  }

  @Test
  public void validateNotAllowedDependenciesInvalid() throws ValidationException {
    Dependency dependencyMock = mock(Dependency.class);

    when(dependencyMock.getGroupId()).thenReturn("fake.group.id");
    when(dependencyMock.getArtifactId()).thenReturn("fake-artifact-id");
    when(dependencyMock.getVersion()).thenReturn("0.0.0");
    when(dependencyMock.getScope()).thenReturn("compile");
    when(dependencyMock.getType()).thenReturn("mule-server-plugin");
    when(dependencyMock.getClassifier()).thenReturn("mule-server-plugin");

    when(projectMock.getDependencies()).thenReturn(singletonList(dependencyMock));
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);

    doCallRealMethod().when(mojoMock).buildArtifactCoordinates(any());
    doCallRealMethod().when(mojoMock).validateNotAllowedDependencies();
    assertThatThrownBy(() -> mojoMock.validateNotAllowedDependencies()).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  public void validateNotAllowedMunitRunnerDependenciesWithCompileScope() throws Exception {

    Dependency dependencyMock = mock(Dependency.class);

    final String version = "0.0.0";

    when(dependencyMock.getGroupId()).thenReturn(MUNIT_GROUP_ID);
    when(dependencyMock.getArtifactId()).thenReturn(MUNIT_RUNNER_ARTIFACT_ID);
    when(dependencyMock.getVersion()).thenReturn(version);
    when(dependencyMock.getScope()).thenReturn("compile");
    when(dependencyMock.getType()).thenReturn("jar");
    when(dependencyMock.getClassifier()).thenReturn("mule-plugin");

    when(projectMock.getDependencies()).thenReturn(singletonList(dependencyMock));
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);

    doCallRealMethod().when(mojoMock).buildArtifactCoordinates(any());
    doCallRealMethod().when(mojoMock).validateNotAllowedDependencies();

    assertThatThrownBy(() -> mojoMock.validateNotAllowedDependencies())
        .isExactlyInstanceOf(ValidationException.class)
        .hasMessageContaining(format("%s:%s:%s", MUNIT_GROUP_ID, MUNIT_RUNNER_ARTIFACT_ID, version))
        .hasMessageContaining("should have scope 'test', found 'compile'");
  }

  @Test
  public void validateNotAllowedMunitToolsDependenciesWithCompileScope() throws Exception {
    Dependency dependencyMock = mock(Dependency.class);

    final String version = "0.0.0";

    when(dependencyMock.getGroupId()).thenReturn(MUNIT_GROUP_ID);
    when(dependencyMock.getArtifactId()).thenReturn(MUNIT_TOOLS_ARTIFACT_ID);
    when(dependencyMock.getVersion()).thenReturn(version);
    when(dependencyMock.getScope()).thenReturn("compile");
    when(dependencyMock.getType()).thenReturn("jar");
    when(dependencyMock.getClassifier()).thenReturn("mule-plugin");

    when(projectMock.getDependencies()).thenReturn(singletonList(dependencyMock));
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);

    doCallRealMethod().when(mojoMock).buildArtifactCoordinates(any());
    doCallRealMethod().when(mojoMock).validateNotAllowedDependencies();

    assertThatThrownBy(() -> mojoMock.validateNotAllowedDependencies())
        .isExactlyInstanceOf(ValidationException.class)
        .hasMessageContaining(format("%s:%s:%s", MUNIT_GROUP_ID, MUNIT_TOOLS_ARTIFACT_ID, version))
        .hasMessageContaining("should have scope 'test', found 'compile'");
  }

  @Test
  public void mUnitDependenciesDoesNotFailWithTestScope() throws Exception {
    Dependency dependencyMock = mock(Dependency.class);

    when(dependencyMock.getGroupId()).thenReturn(MUNIT_GROUP_ID);
    when(dependencyMock.getArtifactId()).thenReturn(MUNIT_TOOLS_ARTIFACT_ID);
    when(dependencyMock.getVersion()).thenReturn("0.0.0");
    when(dependencyMock.getScope()).thenReturn("test");
    when(dependencyMock.getType()).thenReturn("jar");
    when(dependencyMock.getClassifier()).thenReturn("mule-plugin");

    when(projectMock.getDependencies()).thenReturn(singletonList(dependencyMock));
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);

    doCallRealMethod().when(mojoMock).buildArtifactCoordinates(any());
    doCallRealMethod().when(mojoMock).validateNotAllowedDependencies();

    mojoMock.validateNotAllowedDependencies();
  }

}
