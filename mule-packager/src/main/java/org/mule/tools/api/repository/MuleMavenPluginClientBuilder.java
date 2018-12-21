/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.repository;

import static com.google.common.base.Preconditions.checkArgument;
import org.mule.maven.client.api.model.Authentication;
import org.mule.maven.client.api.model.MavenConfiguration;
import org.mule.maven.client.internal.AetherMavenClient;
import org.mule.maven.client.internal.AetherMavenClientProvider;
import org.mule.maven.client.internal.DefaultLocalRepositorySupplierFactory;
import org.mule.maven.client.internal.DefaultSettingsSupplierFactory;
import org.mule.maven.client.internal.MavenEnvironmentVariables;
import org.mule.tools.api.util.PackagerLog;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.repository.AuthenticationContext;
import org.eclipse.aether.repository.RemoteRepository;


public class MuleMavenPluginClientBuilder {

  private PackagerLog log;
  private List<RemoteRepository> remoteRepositories;
  private File localRepository;
  private File globalSettings;
  private File userSettings;
  private Properties userProperties;
  private List<String> activeProfiles;
  private List<String> inactiveProfiles;

  public MuleMavenPluginClientBuilder(PackagerLog log) {
    this.log = log;
  }

  public MuleMavenPluginClientBuilder withRemoteRepositories(List<RemoteRepository> remoteRepositories) {
    checkArgument(remoteRepositories != null, "Remote repositories list can not be null");
    this.remoteRepositories = remoteRepositories;
    return this;
  }

  public MuleMavenPluginClientBuilder withLocalRepository(File localRepository) {
    this.localRepository = localRepository;
    return this;
  }

  public MuleMavenPluginClientBuilder withGlobalSettings(File globalSettings) {
    this.globalSettings = globalSettings;
    return this;
  }

  public MuleMavenPluginClientBuilder withUserProperties(Properties userProperties) {
    this.userProperties = userProperties;
    return this;
  }

  public MuleMavenPluginClientBuilder withActiveProfiles(List<String> activeProfiles) {
    this.activeProfiles = activeProfiles;
    return this;
  }

  public MuleMavenPluginClientBuilder withInactiveProfiles(List<String> inactiveProfiles) {
    this.inactiveProfiles = inactiveProfiles;
    return this;
  }

  public MuleMavenPluginClientBuilder withUserSettings(File userSettings) {
    this.userSettings = userSettings;
    return this;
  }

  public AetherMavenClient build() {
    MavenConfiguration mavenConfiguration = buildMavenConfiguration();
    AetherMavenClientProvider provider = new AetherMavenClientProvider();
    return (AetherMavenClient) provider.createMavenClient(mavenConfiguration);
  }

  protected MavenConfiguration buildMavenConfiguration() {
    MavenConfiguration.MavenConfigurationBuilder mavenConfigurationBuilder = new MavenConfiguration.MavenConfigurationBuilder();
    DefaultSettingsSupplierFactory settingsSupplierFactory = new DefaultSettingsSupplierFactory(new MavenEnvironmentVariables());
    Optional<File> globalSettings = this.globalSettings != null ? Optional.of(this.globalSettings)
        : settingsSupplierFactory.environmentGlobalSettingsSupplier();
    Optional<File> userSettings =
        this.userSettings != null ? Optional.of(this.userSettings) : settingsSupplierFactory.environmentUserSettingsSupplier();
    Optional<File> securitySettings = settingsSupplierFactory.environmentSettingsSecuritySupplier();

    globalSettings.ifPresent(mavenConfigurationBuilder::globalSettingsLocation);
    userSettings.ifPresent(mavenConfigurationBuilder::userSettingsLocation);
    securitySettings.ifPresent(mavenConfigurationBuilder::settingsSecurityLocation);
    mavenConfigurationBuilder.ignoreArtifactDescriptorRepositories(false);
    DefaultLocalRepositorySupplierFactory localRepositorySupplierFactory = new DefaultLocalRepositorySupplierFactory();
    Supplier<File> localMavenRepository =
        localRepository != null ? () -> localRepository : localRepositorySupplierFactory.environmentMavenRepositorySupplier();

    this.remoteRepositories.stream().filter(this::hasValidURL).map(this::toRemoteRepo)
        .forEach(mavenConfigurationBuilder::remoteRepository);

    if (userProperties != null) {
      mavenConfigurationBuilder.userProperties(userProperties);
    }
    if (activeProfiles != null) {
      mavenConfigurationBuilder.activeProfiles(activeProfiles);
    }
    if (inactiveProfiles != null) {
      mavenConfigurationBuilder.inactiveProfiles(inactiveProfiles);
    }

    return mavenConfigurationBuilder
        .localMavenRepositoryLocation(localMavenRepository.get())
        .build();
  }

  private boolean hasValidURL(RemoteRepository remoteRepository) {
    try {
      new URL(remoteRepository.getUrl());
    } catch (MalformedURLException e) {
      return false;
    }
    return true;
  }

  private org.mule.maven.client.api.model.RemoteRepository toRemoteRepo(RemoteRepository remoteRepository) {
    String id = remoteRepository.getId();
    Optional<Authentication> authentication = getAuthentication(remoteRepository);
    URL remoteRepositoryUrl = getURL(remoteRepository);
    org.mule.maven.client.api.model.RemoteRepository.RemoteRepositoryBuilder builder =
        new org.mule.maven.client.api.model.RemoteRepository.RemoteRepositoryBuilder();
    authentication.ifPresent(builder::authentication);
    return builder
        .id(id)
        .url(remoteRepositoryUrl)
        .build();
  }

  private URL getURL(RemoteRepository remoteRepository) {
    try {
      return new URL(remoteRepository.getUrl());
    } catch (MalformedURLException e) {
      log.warn("Could not resolve remote repository URL: " + remoteRepository);
    }
    return null;
  }

  private Optional<Authentication> getAuthentication(RemoteRepository remoteRepository) {
    AuthenticationContext authenticationContext =
        AuthenticationContext.forRepository(new DefaultRepositorySystemSession(), remoteRepository);

    if (authenticationContext == null) {
      return Optional.empty();
    }

    String password = new String(authenticationContext.get(AuthenticationContext.PASSWORD, char[].class));
    String username = new String(authenticationContext.get(AuthenticationContext.USERNAME, char[].class));

    Authentication.AuthenticationBuilder authenticationBuilder = new Authentication.AuthenticationBuilder();
    AuthenticationContext.close(authenticationContext);

    return Optional.of(authenticationBuilder.password(password).username(username).build());
  }
}
