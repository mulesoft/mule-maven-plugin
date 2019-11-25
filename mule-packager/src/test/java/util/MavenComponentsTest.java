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
