/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import integration.test.util.ProjectFactory;
import org.apache.maven.shared.verifier.VerificationException;
import org.apache.maven.shared.verifier.Verifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public abstract class AbstractDeploymentTest {

  protected static final Logger LOG = LoggerFactory.getLogger(AbstractDeploymentTest.class);

  private static final String MAVEN_OPTS = "MAVEN_OPTS";
  private static final String DEFAULT_MULE_VERSION = "4.7.4";
  private static final String MAVEN_OPTS_PROPERTY_KEY = "argLine";

  protected static final String DEPLOY_GOAL = "deploy";
  protected static final String PRODUCTION_ENVIRONMENT = "Production";
  protected static final String USERNAME_ENVIRONMENT_VARIABLE = "username";
  protected static final String PASSWORD_ENVIRONMENT_VARIABLE = "password";

  private static final String USERNAME = System.getProperty(USERNAME_ENVIRONMENT_VARIABLE);
  private static final String PASSWORD = System.getProperty(PASSWORD_ENVIRONMENT_VARIABLE);

  public abstract String getApplication();

  protected String getMuleVersion() {
    return System.getProperty("mule.version", DEFAULT_MULE_VERSION);
  }

  protected String getUsername() {
    return USERNAME;
  }

  public String getPassword() {
    return PASSWORD;
  }

  protected Verifier buildBaseVerifier() throws Exception {
    File projectBaseDirectory = ProjectFactory.createProjectBaseDir(getApplication());
    Verifier verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliArgument("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliArgument("-X");
    return verifier;
  }

  private Verifier buildVerifier(File projectBaseDirectory) throws VerificationException {
    Verifier verifier = new Verifier(projectBaseDirectory.getAbsolutePath());
    setSettings(verifier);
    setMuleMavenPluginVersion(verifier);
    setMavenOpts(verifier);
    return verifier;
  }

  private void setSettings(Verifier verifier) {
    Stream.of("MAVEN_SETTINGS", "settingsxml", "maven.settings")
        .map(key -> Optional.ofNullable(System.getenv(key)).orElseGet(() -> System.getenv(key)))
        .filter(Objects::nonNull)
        .findFirst().ifPresent(mavenSettings -> verifier.addCliArguments("-s " + mavenSettings));
  }

  private void setMuleMavenPluginVersion(Verifier verifier) {
    String projectVersion = System.getProperty("mule.maven.plugin.version");
    if (projectVersion != null) {
      verifier.setSystemProperty("muleMavenPluginVersion", projectVersion);
    }
  }

  private void setMavenOpts(Verifier verifier) {
    String mavenOpts = System.getProperty(MAVEN_OPTS_PROPERTY_KEY);
    if (mavenOpts != null) {
      verifier.setEnvironmentVariable(MAVEN_OPTS, mavenOpts);
      verifier.setSystemProperty(MAVEN_OPTS, mavenOpts);
    }
  }
}
