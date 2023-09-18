/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

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

import java.util.Collections;
import java.util.Properties;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public class TestCompileMojoTest extends AbstractMuleMojoTest {

  private ValidateMojo mojoMock;
  private MavenExecutionRequest mavenExecutionRequestMock;

  private AbstractProjectValidator validatorMock;

  @BeforeEach
  void before() {
    validatorMock = mock(AbstractProjectValidator.class);

    mojoMock = mock(ValidateMojo.class);
    mojoMock.project = projectMock;
    mojoMock.session = mavenSessionMock;
    mojoMock.validator = validatorMock;

    when(mojoMock.getLog()).thenReturn(logMock);

    mavenExecutionRequestMock = mock(MavenExecutionRequest.class);
  }

  @Test
  void executeDoNotVerify()
      throws MojoFailureException, MojoExecutionException, ValidationException {
    mojoMock.skipValidation = true;

    doCallRealMethod().when(mojoMock).execute();
    mojoMock.execute();

    verify(mojoMock, times(0)).validateMavenEnvironment();
  }

  @Test
  void execute()
      throws MojoFailureException, MojoExecutionException, ValidationException {
    when(validatorMock.isProjectValid(any())).thenReturn(true);
    when(mojoMock.getProjectValidator()).thenReturn(validatorMock);


    doCallRealMethod().when(mojoMock).execute();
    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.execute();

    verify(mojoMock, times(1)).validateMavenEnvironment();
  }

  @Test
  void validateMavenEnvironmentValid() throws ValidationException {

    Properties systemProperties = new Properties();
    systemProperties.put("maven.version", "3.3.3");
    when(mavenExecutionRequestMock.getSystemProperties()).thenReturn(systemProperties);
    when(mavenSessionMock.getRequest()).thenReturn(mavenExecutionRequestMock);

    doCallRealMethod().when(mojoMock).validateMavenEnvironment();
    mojoMock.validateMavenEnvironment();
  }

  @Test
  void validateMavenEnvironmentInvalid() throws ValidationException {
    Properties systemProperties = new Properties();
    systemProperties.put("maven.version", "3.3.2");
    when(mavenExecutionRequestMock.getSystemProperties()).thenReturn(systemProperties);
    when(mavenSessionMock.getRequest()).thenReturn(mavenExecutionRequestMock);

    doCallRealMethod().when(mojoMock).validateMavenEnvironment();
    assertThatThrownBy(() -> mojoMock.validateMavenEnvironment()).isExactlyInstanceOf(ValidationException.class);
  }

  @Test
  void validateNotAllowedDependenciesValid() throws ValidationException {
    Dependency dependencyMock = mock(Dependency.class);

    when(dependencyMock.getGroupId()).thenReturn("fake.group.id");
    when(dependencyMock.getArtifactId()).thenReturn("fake-artifact-id");
    when(dependencyMock.getVersion()).thenReturn("0.0.0");
    when(dependencyMock.getScope()).thenReturn("provided");
    when(dependencyMock.getType()).thenReturn("mule-server-plugin");
    when(dependencyMock.getClassifier()).thenReturn("mule-server-plugin");

    when(projectMock.getDependencies()).thenReturn(Collections.singletonList(dependencyMock));
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);

    doCallRealMethod().when(mojoMock).buildArtifactCoordinates(any());
    doCallRealMethod().when(mojoMock).validateNotAllowedDependencies();
    mojoMock.validateNotAllowedDependencies();
  }

  @Test
  void validateNotAllowedDependenciesInvalid() throws ValidationException {
    Dependency dependencyMock = mock(Dependency.class);

    when(dependencyMock.getGroupId()).thenReturn("fake.group.id");
    when(dependencyMock.getArtifactId()).thenReturn("fake-artifact-id");
    when(dependencyMock.getVersion()).thenReturn("0.0.0");
    when(dependencyMock.getScope()).thenReturn("compile");
    when(dependencyMock.getType()).thenReturn("mule-server-plugin");
    when(dependencyMock.getClassifier()).thenReturn("mule-server-plugin");

    when(projectMock.getDependencies()).thenReturn(Collections.singletonList(dependencyMock));
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);

    doCallRealMethod().when(mojoMock).buildArtifactCoordinates(any());
    doCallRealMethod().when(mojoMock).validateNotAllowedDependencies();
    assertThatThrownBy(() -> mojoMock.validateNotAllowedDependencies()).isExactlyInstanceOf(ValidationException.class);
  }

}
