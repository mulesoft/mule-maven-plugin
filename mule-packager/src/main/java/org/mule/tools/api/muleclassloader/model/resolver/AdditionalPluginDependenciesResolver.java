/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.muleclassloader.model.resolver;

import com.vdurmont.semver4j.Semver;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.mule.maven.client.api.MavenClient;
import org.mule.maven.pom.parser.api.model.AdditionalPluginDependencies;
import org.mule.maven.pom.parser.api.model.BundleDependency;
import org.mule.maven.pom.parser.api.model.BundleDescriptor;
import org.mule.runtime.api.exception.MuleRuntimeException;
import org.mule.tools.api.classloader.model.Artifact;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.classloader.model.ClassLoaderModel;
import org.mule.tools.api.muleclassloader.model.util.ArtifactUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vdurmont.semver4j.Semver.SemverType.LOOSE;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static org.mule.maven.pom.parser.api.MavenPomParserProvider.discoverProvider;
import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_PLUGIN;
import static org.mule.tools.deployment.AbstractDeployerFactory.MULE_APPLICATION_CLASSIFIER;

/**
 * Resolves additional plugin libraries for all plugins declared.
 *
 * @since 3.2.0
 */
public class AdditionalPluginDependenciesResolver {

  protected static final String MULE_EXTENSIONS_PLUGIN_GROUP_ID = "org.mule.runtime.plugins";
  protected static final String MULE_EXTENSIONS_PLUGIN_ARTIFACT_ID = "mule-extensions-maven-plugin";
  protected static final String MULE_MAVEN_PLUGIN_GROUP_ID = "org.mule.tools.maven";
  protected static final String MULE_MAVEN_PLUGIN_ARTIFACT_ID = "mule-maven-plugin";
  protected static final String ADDITIONAL_PLUGIN_DEPENDENCIES_ELEMENT = "additionalPluginDependencies";
  protected static final String ADDITIONAL_DEPENDENCIES_ELEMENT = "additionalDependencies";
  protected static final String GROUP_ID_ELEMENT = "groupId";
  protected static final String ARTIFACT_ID_ELEMENT = "artifactId";
  protected static final String VERSION_ELEMENT = "version";
  protected static final String PLUGIN_ELEMENT = "plugin";
  protected static final String DEPENDENCY_ELEMENT = "dependency";
  private final MavenClient mavenClient;
  private final List<AdditionalPluginDependencies> pluginsWithAdditionalDependencies;
  private final File temporaryFolder;

  public AdditionalPluginDependenciesResolver(MavenClient mavenClient,
                                              List<Plugin> additionalPluginDependencies,
                                              File temporaryFolder) {
    this.mavenClient = mavenClient;
    this.pluginsWithAdditionalDependencies = additionalPluginDependencies.stream().map(this::toAdditionalPluginDependencies)
        .collect(Collectors.toCollection(ArrayList::new));
    this.temporaryFolder = temporaryFolder;
  }

  public Map<BundleDependency, List<BundleDependency>> resolveDependencies(List<BundleDependency> applicationDependencies,
                                                                           Collection<ClassLoaderModel> mulePluginsClassLoaderModels) {
    addPluginDependenciesAdditionalLibraries(applicationDependencies);
    Map<BundleDependency, List<BundleDependency>> pluginsWithAdditionalDeps = new LinkedHashMap<>();
    for (AdditionalPluginDependencies pluginWithAdditionalDependencies : pluginsWithAdditionalDependencies) {
      BundleDependency pluginBundleDependency =
          getPluginBundleDependency(pluginWithAdditionalDependencies, applicationDependencies);

      List<Artifact> pluginDependencies =
          getPluginDependencies(pluginWithAdditionalDependencies, mulePluginsClassLoaderModels);

      List<BundleDependency> additionalDependencies =
          resolveDependencies(pluginWithAdditionalDependencies.getAdditionalDependencies().stream()
              .filter(additionalDep -> pluginDependencies.stream().noneMatch(areSameArtifact(additionalDep)))
              .collect(toList()));

      if (!additionalDependencies.isEmpty()) {
        pluginsWithAdditionalDeps.put(pluginBundleDependency, additionalDependencies);
      }
    }
    return pluginsWithAdditionalDeps;
  }

  private List<BundleDependency> resolveDependencies(List<BundleDescriptor> additionalDependencies) {
    return mavenClient.resolveArtifactDependencies(additionalDependencies,
                                                   of(mavenClient.getMavenConfiguration()
                                                       .getLocalMavenRepositoryLocation()),
                                                   empty());
  }

  private BundleDependency getPluginBundleDependency(AdditionalPluginDependencies plugin,
                                                     List<BundleDependency> mulePlugins) {
    Predicate<BundleDependency> match = dependency -> Stream.of(
                                                                Pair.of(dependency.getDescriptor()
                                                                    .getGroupId(),
                                                                        plugin.getGroupId()),
                                                                Pair.of(dependency.getDescriptor()
                                                                    .getArtifactId(),
                                                                        plugin
                                                                            .getArtifactId()))
        .allMatch(pair -> StringUtils.equals(pair.getLeft(), pair.getRight()));

    return mulePlugins.stream()
        .filter(match)
        .findFirst()
        .orElseThrow(() -> new MuleRuntimeException(createStaticMessage("Declared additional dependencies for a plugin not present: "
            + plugin)));
  }

  private List<Artifact> getPluginDependencies(AdditionalPluginDependencies plugin,
                                               Collection<ClassLoaderModel> mulePluginsClassLoaderModels) {
    Function<ArtifactCoordinates, Boolean> mapper = coordinates -> Stream.of(
                                                                             Pair.of(coordinates.getGroupId(),
                                                                                     plugin.getGroupId()),
                                                                             Pair.of(coordinates.getArtifactId(),
                                                                                     plugin.getArtifactId()))
        .allMatch(pair -> StringUtils.equals(pair.getLeft(), pair.getRight()));

    Predicate<ClassLoaderModel> match = classLoaderModel -> Optional.ofNullable(classLoaderModel.getArtifactCoordinates())
        .map(mapper)
        .orElse(false);

    return mulePluginsClassLoaderModels.stream()
        .filter(match)
        .findFirst()
        .map(ClassLoaderModel::getDependencies)
        .orElseThrow(() -> new MuleRuntimeException(createStaticMessage("Could not find ClassLoaderModel resolved for plugin: "
            + plugin)));
  }

  private Predicate<Artifact> areSameArtifact(BundleDescriptor descriptor) {
    return artifact -> StringUtils.equals(descriptor.getArtifactId(), artifact.getArtifactCoordinates().getArtifactId())
        && StringUtils.equals(descriptor.getGroupId(), artifact.getArtifactCoordinates().getGroupId())
        && StringUtils.equals(descriptor.getVersion(), artifact.getArtifactCoordinates().getVersion());
  }

  private void addPluginDependenciesAdditionalLibraries(List<BundleDependency> applicationDependencies) {
    List<BundleDependency> plugins = applicationDependencies
        .stream()
        .filter(bundleDependency -> bundleDependency.getDescriptor().getClassifier().map(MULE_PLUGIN::equals).orElse(false))
        .collect(toList());

    Collection<AdditionalPluginDependencies> additionalDependencies = resolveAdditionalDependenciesFromMulePlugins(plugins);

    pluginsWithAdditionalDependencies.addAll(additionalDependencies.stream()
        .filter(this::isNotRedefinedAtApplicationLevel)
        .collect(toList()));
  }

  private Collection<AdditionalPluginDependencies> resolveAdditionalDependenciesFromMulePlugins(List<BundleDependency> mulePlugins) {
    BiPredicate<BundleDescriptor, BundleDescriptor> match = (obj00, obj01) -> Stream
        .<Function<BundleDescriptor, String>>of(BundleDescriptor::getGroupId, BundleDescriptor::getArtifactId,
                                                BundleDescriptor::getType, value -> value.getClassifier().orElse(null))
        .allMatch(getter -> StringUtils.equals(getter.apply(obj00), getter.apply(obj01)));

    Map<String, AdditionalPluginDependencies> additionalDependencies = new HashMap<>();
    mulePlugins.stream()
        .map(mulePlugin -> new File(mulePlugin.getBundleUri()))
        .filter(file -> mavenClient.getRawPomModel(file).getPackaging().equals(MULE_APPLICATION_CLASSIFIER))
        .map(file -> mavenClient.getEffectiveModel(file, of(temporaryFolder)).getPomFile())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(pomFile -> discoverProvider()
            .createMavenPomParserClient(pomFile.toPath()).getPomAdditionalPluginDependenciesForArtifacts().values()
            .forEach(mavenPlugin -> {
              String artifact = mavenPlugin.getGroupId() + ":" + mavenPlugin.getArtifactId();
              AdditionalPluginDependencies alreadyDefinedDependencies = additionalDependencies.get(artifact);
              if (Objects.nonNull(alreadyDefinedDependencies)) {
                List<BundleDescriptor> effectiveDependencies =
                    new LinkedList<>(alreadyDefinedDependencies.getAdditionalDependencies());
                mavenPlugin.getAdditionalDependencies().forEach(additionalDependency -> {
                  boolean addDependency = true;
                  for (BundleDescriptor effectiveDependency : effectiveDependencies) {
                    if (match.test(effectiveDependency, additionalDependency)) {
                      if (isNewerVersion(additionalDependency.getVersion(), effectiveDependency.getVersion())) {
                        effectiveDependencies.remove(effectiveDependency);
                      } else {
                        addDependency = false;
                      }
                      break;
                    }
                  }
                  if (addDependency) {
                    effectiveDependencies.add(additionalDependency);
                  }
                });
                additionalDependencies
                    .replace(artifact, new AdditionalPluginDependencies(alreadyDefinedDependencies, effectiveDependencies));
              } else {
                additionalDependencies.put(mavenPlugin.getGroupId() + ":" + mavenPlugin.getArtifactId(), mavenPlugin);
              }
            }));

    return additionalDependencies.values();
  }

  private boolean isNewerVersion(String dependencyA, String dependencyB) {
    try {
      return new Semver(dependencyA, LOOSE).isGreaterThan(new Semver(dependencyB, LOOSE));
    } catch (IllegalArgumentException e) {
      // If not using semver lets just compare the strings.
      return dependencyA.compareTo(dependencyB) > 0;
    }
  }

  private boolean isNotRedefinedAtApplicationLevel(AdditionalPluginDependencies additionalPluginDependencies) {
    BiPredicate<AdditionalPluginDependencies, AdditionalPluginDependencies> match = (obj00, obj01) -> Stream
        .<Function<AdditionalPluginDependencies, String>>of(AdditionalPluginDependencies::getGroupId,
                                                            AdditionalPluginDependencies::getArtifactId)
        .allMatch(getter -> StringUtils.equals(getter.apply(obj00), getter.apply(obj01)));

    return pluginsWithAdditionalDependencies.stream()
        .noneMatch(additionalDependency -> match.test(additionalPluginDependencies, additionalDependency));
  }

  private AdditionalPluginDependencies toAdditionalPluginDependencies(Plugin plugin) {
    return new AdditionalPluginDependencies(plugin.getGroupId(), plugin.getArtifactId(),
                                            Optional.ofNullable(plugin.getAdditionalDependencies()).map(List::stream)
                                                .orElse(Stream.empty()).map(ArtifactUtils::toBundleDescriptor).collect(toList()));
  }
}
