/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package util;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.mule.tools.api.util.MavenComponents;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MavenComponentsTest {

  @Test
  void nullLog() {
    testException(() -> new MavenComponents().withLog(null));
  }

  @Test
  void nullProject() {
    testException(() -> new MavenComponents().withProject(null));
  }

  @Test
  void nullOutputDirectory() {
    testException(() -> new MavenComponents().withOutputDirectory(null));
  }

  @Test
  void nullSession() {
    testException(() -> new MavenComponents().withSession(null));
  }

  @Test
  void nullSharedLibraries() {
    testException(() -> new MavenComponents().withSharedLibraries(null));
  }

  @Test
  void nullProjectBuilder() {
    testException(() -> new MavenComponents().withProjectBuilder(null));
  }

  @Test
  void nullRepositorySystem() {
    testException(() -> new MavenComponents().withRepositorySystem(null));
  }

  @Test
  void nullLocalRepository() {
    testException(() -> new MavenComponents().withLocalRepository(null));
  }

  @Test
  void nullRemoteArtifactRepository() {
    testException(() -> new MavenComponents().withRemoteArtifactRepositories(null));
  }

  @Test
  void nullProjectBaseFolder() {
    testException(() -> new MavenComponents().withProjectBaseFolder(null));
  }

  private void testException(ThrowableAssert.ThrowingCallable callable) {
    assertThatThrownBy(callable).isExactlyInstanceOf(IllegalArgumentException.class);
  }
}
