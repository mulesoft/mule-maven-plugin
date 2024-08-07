/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import org.apache.maven.shared.verifier.VerificationException;
import org.apache.maven.shared.verifier.Verifier;

import java.io.File;

public interface SettingsConfigurator {

  default void setSettings(Verifier verifier) {
    String mavenSettings = System.getenv("MAVEN_SETTINGS");
    if (mavenSettings != null) {
      verifier.addCliArguments("-s " + mavenSettings);
    }
  }

  default void setMuleMavenPluginVersion(Verifier verifier) {
    String projectVersion = System.getProperty("mule.maven.plugin.version");
    if (projectVersion != null) {
      verifier.setSystemProperty("muleMavenPluginVersion", projectVersion);
    }
  }

  default void setMavenOpts(Verifier verifier) {
    String mavenOpts = System.getProperty("argLine");
    if (mavenOpts != null) {
      verifier.setEnvironmentVariable("MAVEN_OPTS", mavenOpts);
    }
  }

  default Verifier buildVerifier(File projectBaseDirectory) throws VerificationException {
    Verifier verifier = new Verifier(projectBaseDirectory.getAbsolutePath());
    setSettings(verifier);
    setMuleMavenPluginVersion(verifier);
    setMavenOpts(verifier);
    return verifier;
  }
}
