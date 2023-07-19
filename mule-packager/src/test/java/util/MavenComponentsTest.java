/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package util;

import org.mule.tools.api.util.MavenComponents;

import org.junit.Test;

public class MavenComponentsTest {

  @Test(expected = IllegalArgumentException.class)
  public void nullLog() {
    new MavenComponents().withLog(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullProject() {
    new MavenComponents().withProject(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullOutputDirectory() {
    new MavenComponents().withOutputDirectory(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullSession() {
    new MavenComponents().withSession(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullSharedLibraries() {
    new MavenComponents().withSharedLibraries(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullProjectBuilder() {
    new MavenComponents().withProjectBuilder(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullRepositorySystem() {
    new MavenComponents().withRepositorySystem(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullLocalRepository() {
    new MavenComponents().withLocalRepository(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullRemoteArtifactRepository() {
    new MavenComponents().withRemoteArtifactRepositories(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void nullProjectBaseFolder() {
    new MavenComponents().withProjectBaseFolder(null);
  }
}
