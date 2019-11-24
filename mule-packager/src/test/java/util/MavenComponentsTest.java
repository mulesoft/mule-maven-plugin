/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package util;

import static org.mockito.Mockito.mock;
import org.mule.tools.api.util.MavenComponents;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;
import org.junit.Test;

public class MavenComponentsTest {

  @Test(expected = IllegalArgumentException.class)
  public void nullLog() {
    new MavenComponents(null, mock(MavenProject.class), mock(File.class), mock(MavenSession.class), mock(List.class),
                        mock(ProjectBuilder.class), mock(RepositorySystem.class), mock(ArtifactRepository.class),
                        mock(List.class),
                        null, mock(List.class), mock(File.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullProject() {
    new MavenComponents(mock(Log.class), null, mock(File.class), mock(MavenSession.class), mock(List.class),
                        mock(ProjectBuilder.class), mock(RepositorySystem.class), mock(ArtifactRepository.class),
                        mock(List.class), null,
                        mock(List.class), mock(File.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullOutputDirectory() {
    new MavenComponents(mock(Log.class), mock(MavenProject.class), null, mock(MavenSession.class), mock(List.class),
                        mock(ProjectBuilder.class), mock(RepositorySystem.class), mock(ArtifactRepository.class),
                        mock(List.class),
                        null, mock(List.class), mock(File.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullSession() {
    new MavenComponents(mock(Log.class), mock(MavenProject.class), mock(File.class), null, mock(List.class),
                        mock(ProjectBuilder.class), mock(RepositorySystem.class), mock(ArtifactRepository.class),
                        mock(List.class), null,
                        mock(List.class), mock(File.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullSharedLibraries() {
    new MavenComponents(mock(Log.class), mock(MavenProject.class), mock(File.class), mock(MavenSession.class), null,
                        mock(ProjectBuilder.class), mock(RepositorySystem.class),
                        mock(ArtifactRepository.class), mock(List.class), null, mock(List.class), mock(File.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullProjectBuilder() {
    new MavenComponents(mock(Log.class), mock(MavenProject.class), mock(File.class), mock(MavenSession.class), mock(List.class),
                        null, mock(RepositorySystem.class),
                        mock(ArtifactRepository.class), mock(List.class), null, mock(List.class), mock(File.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullRepositorySystem() {
    new MavenComponents(mock(Log.class), mock(MavenProject.class), mock(File.class), mock(MavenSession.class), mock(List.class),
                        mock(ProjectBuilder.class), null,
                        mock(ArtifactRepository.class), mock(List.class), null, mock(List.class), mock(File.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullLocalRepository() {
    new MavenComponents(mock(Log.class), mock(MavenProject.class), mock(File.class), mock(MavenSession.class), mock(List.class),
                        mock(ProjectBuilder.class), mock(RepositorySystem.class),
                        null, mock(List.class), null, mock(List.class), mock(File.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullRemoteArtifactRepository() {
    new MavenComponents(mock(Log.class), mock(MavenProject.class), mock(File.class), mock(MavenSession.class), mock(List.class),
                        mock(ProjectBuilder.class), mock(RepositorySystem.class),
                        mock(ArtifactRepository.class), null, null, mock(List.class), mock(File.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullProjectBaseFolder() {
    new MavenComponents(mock(Log.class), mock(MavenProject.class), mock(File.class), mock(MavenSession.class), mock(List.class),
                        mock(ProjectBuilder.class), mock(RepositorySystem.class),
                        mock(ArtifactRepository.class), mock(List.class), null, mock(List.class), null);
  }
}
