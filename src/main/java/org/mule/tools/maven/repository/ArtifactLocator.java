/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.repository;

import static java.lang.String.format;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.repository.AuthenticationContext;
import org.eclipse.aether.repository.RemoteRepository;
import org.mule.maven.client.api.BundleDescriptorCreationException;
import org.mule.maven.client.api.MavenClient;
import org.mule.maven.client.api.model.Authentication;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.api.model.MavenConfiguration;
import org.mule.maven.client.api.model.RemoteRepository.RemoteRepositoryBuilder;
import org.mule.maven.client.internal.*;


public class ArtifactLocator {

  private static final String POM = "pom";
  private final List<RemoteRepository> remoteRepositories;
  private final Log log;
  private MavenProject project;
  private ArtifactRepository localRepository;
  private MavenProjectBuilder mavenProjectBuilder;
  private Set<Artifact> artifacts;

  public ArtifactLocator(List<RemoteRepository> remoteRepositories, MavenProject project, ArtifactRepository localRepository,
                         MavenProjectBuilder mavenProjectBuilder, Log log) {
    this.remoteRepositories = remoteRepositories;
    this.project = project;
    this.localRepository = localRepository;
    this.mavenProjectBuilder = mavenProjectBuilder;
    this.log = log;
    artifacts = new HashSet<>();
  }


  public Set<Artifact> getArtifacts(File pomFile, File targetFolder)
      throws MojoExecutionException {
    AetherMavenClient client = (AetherMavenClient) buildMavenClient();
    List<BundleDependency> dependencies = client.resolveApplicationBundleDependencies(pomFile, targetFolder);
    dependencies.forEach(dependency -> artifacts.add(buildArtifact(dependency)));
    for (Artifact dep : new ArrayList<>(artifacts)) {
      addThirdPartyParentPomArtifacts(artifacts, dep);
    }
    addParentPomArtifacts(artifacts);
    return new HashSet<>(artifacts);
  }

  private Artifact buildArtifact(BundleDependency dependency) {
    BundleDescriptor descriptor = dependency.getDescriptor();
    Artifact artifact = new DefaultArtifact(descriptor.getGroupId(), descriptor.getArtifactId(), descriptor.getVersion(), null,
                                            descriptor.getType(),
                                            descriptor.getClassifier().orElse(null),
                                            new DefaultArtifactHandler());
    artifact.setFile(new File(dependency.getBundleUri().getPath()));
    return artifact;
  }

  protected MavenClient buildMavenClient() {
    MavenConfiguration mavenConfiguration = buildMavenConfiguration();
    AetherMavenClientProvider provider = new AetherMavenClientProvider();
    return provider.createMavenClient(mavenConfiguration);
  }

  public MavenConfiguration buildMavenConfiguration() {
    MavenConfiguration.MavenConfigurationBuilder mavenConfigurationBuilder = new MavenConfiguration.MavenConfigurationBuilder();

    DefaultSettingsSupplierFactory settingsSupplierFactory = new DefaultSettingsSupplierFactory(new MavenEnvironmentVariables());
    Optional<File> globalSettings = settingsSupplierFactory.environmentGlobalSettingsSupplier();
    Optional<File> userSettings = settingsSupplierFactory.environmentUserSettingsSupplier();

    globalSettings.ifPresent(mavenConfigurationBuilder::withGlobalSettingsLocation);
    userSettings.ifPresent(mavenConfigurationBuilder::withUserSettingsLocation);

    DefaultLocalRepositorySupplierFactory localRepositorySupplierFactory = new DefaultLocalRepositorySupplierFactory();
    Supplier<File> localMavenRepository = localRepositorySupplierFactory.environmentMavenRepositorySupplier();

    this.remoteRepositories.stream().map(this::toRemoteRepo).forEach(mavenConfigurationBuilder::withRemoteRepository);

    return mavenConfigurationBuilder
        .withLocalMavenRepositoryLocation(localMavenRepository.get())
        .build();
  }

  private org.mule.maven.client.api.model.RemoteRepository toRemoteRepo(RemoteRepository remoteRepository) {
    String id = remoteRepository.getId();
    Optional<Authentication> authentication = getAuthentication(remoteRepository);
    URL url = null;
    try {
      url = getURL(remoteRepository);
    } catch (MavenExecutionException e) {
      e.printStackTrace();
    }
    RemoteRepositoryBuilder builder = new RemoteRepositoryBuilder();
    authentication.ifPresent(builder::withAuthentication);
    return builder
        .withId(id)
        .withUrl(url)
        .build();
  }

  private URL getURL(RemoteRepository remoteRepository) throws MavenExecutionException {
    try {
      return new URL(remoteRepository.getUrl());
    } catch (MalformedURLException e) {
      throw new MavenExecutionException(e.getMessage(), e.getCause());
    }
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

    return Optional.of(authenticationBuilder.withPassword(password).withUsername(username).build());
  }

  private static Model createPomModel(File pomFile) {
    MavenXpp3Reader reader = new MavenXpp3Reader();
    Model model;
    try (FileReader mulePluginPomFilerReader = new FileReader(pomFile)) {
      model = reader.read(mulePluginPomFilerReader);
    } catch (IOException | XmlPullParserException e) {
      throw new BundleDescriptorCreationException(format("There was an issue reading '%s' in '%s'",
                                                         pomFile.getName(), pomFile.getParentFile().getAbsolutePath()),
                                                  e);
    }
    return model;
  }

  protected void addParentPomArtifacts(Set<Artifact> artifacts) throws MojoExecutionException {
    MavenProject currentProject = project;
    boolean projectParent = true;
    while (currentProject.hasParent() && projectParent) {
      currentProject = currentProject.getParent();
      if (currentProject.getFile() == null) {
        projectParent = false;
      } else {
        Artifact pomArtifact = currentProject.getArtifact();
        pomArtifact.setFile(currentProject.getFile());
        validatePomArtifactFile(pomArtifact);
        if (!artifacts.add(pomArtifact)) {
          break;
        }
      }
    }
    if (!projectParent) {
      final Artifact unresolvedParentPomArtifact = currentProject.getArtifact();
      addThirdPartyParentPomArtifacts(artifacts, unresolvedParentPomArtifact);
    }
  }

  protected void validatePomArtifactFile(Artifact resolvedPomArtifact) throws MojoExecutionException {
    if (resolvedPomArtifact.getFile() == null) {
      throw new MojoExecutionException(
                                       format("There was a problem trying to resolve the artifact's file location for [%s], file was null",
                                              resolvedPomArtifact.toString()));
    }
    if (!resolvedPomArtifact.getFile().exists()) {
      throw new MojoExecutionException(
                                       format("There was a problem trying to resolve the artifact's file location for [%s], file [%s] doesn't exist",
                                              resolvedPomArtifact.toString(), resolvedPomArtifact.getFile().getAbsolutePath()));
    }
  }

  protected void addThirdPartyParentPomArtifacts(Set<Artifact> artifacts, Artifact dep) throws MojoExecutionException {
    MavenProject project = mavenProjectBuilder.buildProjectFromArtifact(dep);
    addParentDependencyPomArtifacts(project, artifacts);

    Artifact pomArtifact = mavenProjectBuilder.createProjectArtifact(dep);
    artifacts.add(getResolvedArtifactUsingLocalRepository(pomArtifact));
  }

  protected Artifact getResolvedArtifactUsingLocalRepository(Artifact pomArtifact) throws MojoExecutionException {
    final Artifact resolvedPomArtifact = localRepository.find(pomArtifact);
    validatePomArtifactFile(resolvedPomArtifact);
    return resolvedPomArtifact;
  }

  protected void addParentDependencyPomArtifacts(MavenProject projectDependency, Set<Artifact> artifacts)
      throws MojoExecutionException {
    MavenProject currentProject = projectDependency;
    while (currentProject.hasParent()) {
      currentProject = currentProject.getParent();
      final Artifact pomArtifact = currentProject.getArtifact();
      if (!artifacts.add(getResolvedArtifactUsingLocalRepository(pomArtifact))) {
        break;
      }
    }
  }

  public Set<Artifact> getExclusions() throws MojoExecutionException {
    Set<Artifact> exclusions = new HashSet<>();
    List<Dependency> directDependencies = project.getDependencies();
    Map<String, Dependency> directDependenciesCoordinates =
        directDependencies.stream().collect(Collectors.toMap(this::getCoordinates, Function.identity()));
    for (Artifact artifact : project.getArtifacts()) {
      if (directDependenciesCoordinates.containsKey(artifact.toString())) {
        Dependency dependency = directDependenciesCoordinates.get(artifact.toString());
        if (dependency.getExclusions() != null && dependency.getExclusions().size() > 0) {
          MavenProject artifactProject = mavenProjectBuilder.buildProjectFromArtifact(artifact);
          exclusions.addAll(findExcludedArtifacts(artifactProject, dependency.getExclusions()));
        }
      }
    }
    return new HashSet<>(exclusions);
  }

  private Set<Artifact> findExcludedArtifacts(MavenProject project, List<Exclusion> exclusions) {
    Set<Artifact> excludedArtifacts = new HashSet<>();
    for (Artifact artifact : project.getArtifacts()) {
      for (Exclusion exclusion : exclusions) {
        if (exclusion.getArtifactId().equals(artifact.getArtifactId()) && exclusion.getGroupId().equals(artifact.getGroupId())) {
          excludedArtifacts.add(artifact);
        }
      }
    }
    for (Dependency dependency : project.getDependencies()) {
      for (Exclusion exclusion : exclusions) {
        if (exclusion.getArtifactId().equals(dependency.getArtifactId())
            && exclusion.getGroupId().equals(dependency.getGroupId())) {
          excludedArtifacts.add(createArtifact(dependency));
        }
      }
    }
    excludedArtifacts.removeAll(artifacts);
    return excludedArtifacts;
  }

  private Artifact createArtifact(Dependency dependency) {
    return new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(),
                               dependency.getScope(), dependency.getType(), dependency.getClassifier(),
                               new DefaultArtifactHandler(dependency.getType()));
  }

  private String getCoordinates(Dependency dependency) {
    return dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getType() + ":"
        + dependency.getVersion() + ":" + dependency.getScope();
  }
}
