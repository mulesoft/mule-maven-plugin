/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.mule.maven.client.api.MavenClient;
import org.mule.maven.client.api.model.MavenConfiguration;
import org.mule.maven.client.api.model.RemoteRepository;
import org.mule.maven.client.internal.MuleMavenClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MavenClientTest {

  public static final String USER_HOME_PROP = "user.home";
  public static final String M2_DIR = ".m2";
  public static final String M2_HOME = "M2_HOME";
  public static final String M2_REPO = "M2_REPO";
  public static final String USER_SETTINGS = "maven.settings";
  public static final String SETTINGS_SECURITY = "maven.settingsSecurity";

  public File getM2Home() throws IOException {
    String mavenHome = System.getenv(M2_HOME);
    if (StringUtils.isBlank(mavenHome)) {
      mavenHome = Paths.get(System.getProperty(USER_HOME_PROP)).resolve(M2_DIR).toFile().getAbsolutePath();
    }
    final File file = Paths.get(mavenHome).toFile();
    if (!file.exists()) {
      Files.createDirectories(file.toPath());
    }
    return file;
  }

  public File getM2Repo(File m2Home) throws IOException {
    String m2Repo = System.getenv(M2_REPO);
    if (StringUtils.isBlank(m2Repo)) {
      m2Repo = Paths.get(m2Home.getAbsolutePath()).resolve("repository").toFile().getAbsolutePath();
    }
    final File file = new File(m2Repo);
    if (!file.exists()) {
      Files.createDirectories(file.toPath());
    }
    return file;
  }

  public File getUserSettings(File m2Repo) {
    return getOverriddenOrDefault(m2Repo, USER_SETTINGS, "settings.xml");
  }

  private File getOverriddenOrDefault(File m2Repo, String property, String defaultPathInM2) {
    String userOverriddenValue = System.getProperty(property);
    if (StringUtils.isEmpty(userOverriddenValue)) {
      final File settings = new File(m2Repo.getParentFile(), defaultPathInM2);
      if (settings.exists()) {
        return settings;
      }
    } else {
      final File file = new File(userOverriddenValue);
      if (file.exists()) {
        return file;
      }
    }
    return null;
  }

  public File getSettingsSecurity(File m2Repo) {
    return getOverriddenOrDefault(m2Repo, SETTINGS_SECURITY, "settings-security.xml");
  }

  public MavenConfiguration.MavenConfigurationBuilder getMavenConfiguration(File m2Repo, File userSettings, File settingsSecurity)
      throws MalformedURLException {
    final MavenConfiguration.MavenConfigurationBuilder mavenConfigurationBuilder =
        new MavenConfiguration.MavenConfigurationBuilder().localMavenRepositoryLocation(m2Repo);

    Optional.ofNullable(userSettings).ifPresent(mavenConfigurationBuilder::userSettingsLocation);
    Optional.ofNullable(settingsSecurity).ifPresent(mavenConfigurationBuilder::settingsSecurityLocation);
    // Needed to take into account repositories declared in the pom.xml of the project and it's dependencies.
    configureMavenCentralRepo(mavenConfigurationBuilder);

    mavenConfigurationBuilder.ignoreArtifactDescriptorRepositories(false);
    return mavenConfigurationBuilder;
  }

  private void configureMavenCentralRepo(MavenConfiguration.MavenConfigurationBuilder mavenConfigurationBuilder)
      throws MalformedURLException {
    mavenConfigurationBuilder.remoteRepository(RemoteRepository.newRemoteRepositoryBuilder().id("central")
        .url(new URL("https://repo.maven.apache.org/maven2/")).build());
  }

  public MavenClient getMavenClientInstance(MavenConfiguration.MavenConfigurationBuilder configurationBuilder) {
    return new MuleMavenClient(configurationBuilder.build());
  }
}
