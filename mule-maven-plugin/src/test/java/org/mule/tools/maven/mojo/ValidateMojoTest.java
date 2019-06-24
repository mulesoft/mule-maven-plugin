/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.validation.project.AbstractProjectValidator;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ValidateMojoTest extends AbstractMuleMojoTest {

  private static final String MUNIT_RUNNER_ARTIFACT_ID = "munit-runner";
  private static final String MUNIT_TOOLS_ARTIFACT_ID = "munit-tools";
  private static final String MUNIT_GROUP_ID = "com.mulesoft.munit";

  private ValidateMojo mojoMock;
  private MavenExecutionRequest mavenExecutionRequestMock;

  private AbstractProjectValidator validatorMock;

  @Rule
  public ExpectedException expectedException = none();

  @Before
  public void before() throws IOException {
    validatorMock = mock(AbstractProjectValidator.class);

    mojoMock = mock(ValidateMojo.class);
    mojoMock.project = projectMock;
    mojoMock.session = mavenSessionMock;
    mojoMock.validator = validatorMock;

    when(mojoMock.getLog()).thenReturn(logMock);

    mavenExecutionRequestMock = mock(MavenExecutionRequest.class);
  }

  @Test
  public void executeDoNotVerify()
      throws MojoFailureException, MojoExecutionException, IOException, ValidationException {
    mojoMock.skipValidation = true;

    doCallRealMethod().when(mojoMock).execute();
    mojoMock.execute();

    verify(mojoMock, times(0)).validateMavenEnvironment();
  }

  @Test
  public void execute()
      throws MojoFailureException, MojoExecutionException, IOException, ValidationException {
    when(validatorMock.isProjectValid(any())).thenReturn(true);
    when(mojoMock.getProjectValidator()).thenReturn(validatorMock);


    doCallRealMethod().when(mojoMock).execute();
    doCallRealMethod().when(mojoMock).doExecute();
    mojoMock.execute();

    verify(mojoMock, times(1)).validateMavenEnvironment();
  }

  @Test
  public void validateMavenEnvironmentValid()
      throws MojoFailureException, MojoExecutionException, IOException, ValidationException {

    Properties systemProperties = new Properties();
    systemProperties.put("maven.version", "3.3.3");
    when(mavenExecutionRequestMock.getSystemProperties()).thenReturn(systemProperties);
    when(mavenSessionMock.getRequest()).thenReturn(mavenExecutionRequestMock);

    doCallRealMethod().when(mojoMock).validateMavenEnvironment();
    mojoMock.validateMavenEnvironment();
  }

  @Test(expected = ValidationException.class)
  public void validateMavenEnvironmentInvalid()
      throws MojoFailureException, MojoExecutionException, IOException, ValidationException {

    Properties systemProperties = new Properties();
    systemProperties.put("maven.version", "3.3.2");
    when(mavenExecutionRequestMock.getSystemProperties()).thenReturn(systemProperties);
    when(mavenSessionMock.getRequest()).thenReturn(mavenExecutionRequestMock);

    doCallRealMethod().when(mojoMock).validateMavenEnvironment();
    mojoMock.validateMavenEnvironment();
  }

  @Test
  public void validateNotAllowedDependenciesValid()
      throws MojoFailureException, MojoExecutionException, IOException, ValidationException {
    Dependency dependencyMock = mock(Dependency.class);

    when(dependencyMock.getGroupId()).thenReturn("fake.group.id");
    when(dependencyMock.getArtifactId()).thenReturn("fake-artifact-id");
    when(dependencyMock.getVersion()).thenReturn("0.0.0");
    when(dependencyMock.getScope()).thenReturn("provided");
    when(dependencyMock.getType()).thenReturn("mule-server-plugin");
    when(dependencyMock.getClassifier()).thenReturn("mule-server-plugin");

    when(projectMock.getDependencies()).thenReturn(Arrays.asList(dependencyMock));
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION.toString());

    doCallRealMethod().when(mojoMock).buildArtifactCoordinates(any());
    doCallRealMethod().when(mojoMock).validateNotAllowedDependencies();
    mojoMock.validateNotAllowedDependencies();
  }

  @Test(expected = ValidationException.class)
  public void validateNotAllowedDependenciesInvalid()
      throws MojoFailureException, MojoExecutionException, IOException, ValidationException {

    Dependency dependencyMock = mock(Dependency.class);

    when(dependencyMock.getGroupId()).thenReturn("fake.group.id");
    when(dependencyMock.getArtifactId()).thenReturn("fake-artifact-id");
    when(dependencyMock.getVersion()).thenReturn("0.0.0");
    when(dependencyMock.getScope()).thenReturn("compile");
    when(dependencyMock.getType()).thenReturn("mule-server-plugin");
    when(dependencyMock.getClassifier()).thenReturn("mule-server-plugin");

    when(projectMock.getDependencies()).thenReturn(Arrays.asList(dependencyMock));
    when(projectMock.getPackaging()).thenReturn(MULE_APPLICATION);

    doCallRealMethod().when(mojoMock).buildArtifactCoordinates(any());
    doCallRealMethod().when(mojoMock).validateNotAllowedDependencies();
    mojoMock.validateNotAllowedDependencies();
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

    expectedException.expect(ValidationException.class);
    expectedException.expectMessage(format("%s:%s:%s", MUNIT_GROUP_ID, MUNIT_RUNNER_ARTIFACT_ID, version));
    expectedException.expectMessage("should have scope \'test\', found \'compile\'");
    mojoMock.validateNotAllowedDependencies();
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

    expectedException.expect(ValidationException.class);
    expectedException.expectMessage(format("%s:%s:%s", MUNIT_GROUP_ID, MUNIT_TOOLS_ARTIFACT_ID, version));
    expectedException.expectMessage("should have scope \'test\', found \'compile\'");
    mojoMock.validateNotAllowedDependencies();
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
