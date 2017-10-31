/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo.deploy;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class AbstractMuleDeployerMojoTest {

  private AbstractMuleDeployerMojo mojoSpy;
  private MavenProject mavenProjectMock;
  private Artifact artifactMock;
  private List<Artifact> attachedArtifacts;


  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    mojoSpy = spy(AbstractMuleDeployerMojo.class);
    mavenProjectMock = mock(MavenProject.class);
    artifactMock = mock(Artifact.class);
    attachedArtifacts = new ArrayList<>();
  }

  @Test
  public void validateIsDeployableMuleDomainTest() throws MojoExecutionException {
    expectedException.expect(MojoExecutionException.class);
    expectedException.expectMessage("Cannot deploy a mule-domain project");

    when(artifactMock.getClassifier()).thenReturn("mule-domain");
    attachedArtifacts.add(artifactMock);

    when(mavenProjectMock.getAttachedArtifacts()).thenReturn(attachedArtifacts);

    mojoSpy.mavenProject = mavenProjectMock;

    mojoSpy.validateIsDeployable();
  }

  @Test
  public void validateIsDeployableMuleApplicationTest() throws MojoExecutionException {
    when(artifactMock.getClassifier()).thenReturn("mule-application");
    attachedArtifacts.add(artifactMock);

    when(mavenProjectMock.getAttachedArtifacts()).thenReturn(attachedArtifacts);

    mojoSpy.mavenProject = mavenProjectMock;

    mojoSpy.validateIsDeployable();
  }
}
