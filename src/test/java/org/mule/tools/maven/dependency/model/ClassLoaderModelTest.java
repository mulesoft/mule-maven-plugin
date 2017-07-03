/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.dependency.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomNumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mule.maven.client.internal.AetherMavenClient.MULE_PLUGIN_CLASSIFIER;

public class ClassLoaderModelTest {

  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String DEFAULT_ARTIFACT_DESCRIPTOR_TYPE = "jar";
  private static final int LENGTH = 5;
  private static final int NUM_DIGITS = 1;
  private static final String NOT_MULE_PLUGIN_EXAMPLE_CLASSIFIER = "javadoc";
  private static final String MULE_APPLICATION_CLASSIFIER = "mule-application";
  private static final String ARTIFACT_ID_SEPARATOR = "-";
  private static final String VERSION_SEPARATOR = ".";
  private static final String GROUP_ID_PREFIX = "com.";
  private static final String URI_SEPARATOR = "/";
  private static final int INITIAL_NUMBER_DEPENDENCIES = 2;
  private static final int ADDITIONAL_NUMBER_DEPENDENCIES = 4;
  private static final int ARBITRARY_NUMBER_DEPENDENCIES = 3;
  private static final int NUMBER_MULE_PLUGINS = 2;
  private static final int NUMBER_NOT_MULE_PLUGINS_DEPENDENCIES = 2;
  private static final int NUMBER_DEPENDENCIES_WITHOUT_CLASSIFIER = 4;

  private ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void checkNullVersionTest() {
    expectedException.expect(IllegalArgumentException.class);
    new ClassLoaderModel(null, artifactCoordinates);
  }

  @Test
  public void checkNullArtifactCoordinatesTest() {
    expectedException.expect(IllegalArgumentException.class);
    new ClassLoaderModel(VERSION, null);
  }

  @Test
  public void checkNullArgumentsTest() {
    expectedException.expect(IllegalArgumentException.class);
    new ClassLoaderModel(null, null);
  }

  @Test
  public void validatePluginsTest() throws URISyntaxException {
    expectedException.expect(IllegalArgumentException.class);
    String notMulePluginsInitialMessage = "The following dependencies are not mule plugins but are trying to be added as such: ";
    expectedException.expectMessage(notMulePluginsInitialMessage);

    Set<Dependency> mulePlugins = buildDependencySet(NUMBER_MULE_PLUGINS, MULE_PLUGIN_CLASSIFIER);
    Set<Dependency> notMulePlugins1 =
        buildDependencySet(NUMBER_NOT_MULE_PLUGINS_DEPENDENCIES, NOT_MULE_PLUGIN_EXAMPLE_CLASSIFIER);
    Set<Dependency> notMulePlugins2 = buildDependencySet(NUMBER_DEPENDENCIES_WITHOUT_CLASSIFIER, null);

    Set<Dependency> allDependenciesCandidatesAsMulePlugins = new HashSet<>();

    allDependenciesCandidatesAsMulePlugins.addAll(mulePlugins);
    allDependenciesCandidatesAsMulePlugins.addAll(notMulePlugins1);
    allDependenciesCandidatesAsMulePlugins.addAll(notMulePlugins2);

    ClassLoaderModel model = new ClassLoaderModel(VERSION, buildArtifactCoordinates(MULE_APPLICATION_CLASSIFIER));

    // 2 (NUMBER_MULE_PLUGINS) dependencies are mule plugins; the remaining 6 (NUMBER_NOT_MULE_PLUGINS_DEPENDENCIES +
    // NUMBER_DEPENDENCIES_WITHOUT_CLASSIFIER) are not
    model.validatePlugins(allDependenciesCandidatesAsMulePlugins);
  }

  @Test
  public void addMulePluginTest() throws URISyntaxException {
    Dependency mulePlugin1 = buildDependency(MULE_PLUGIN_CLASSIFIER);
    Dependency mulePlugin2 = buildDependency(MULE_PLUGIN_CLASSIFIER);

    SortedSet<Dependency> mulePlugin1Dependencies = new TreeSet<>(buildDependencySet(ARBITRARY_NUMBER_DEPENDENCIES, null));
    SortedSet<Dependency> mulePlugin2Dependencies = new TreeSet<>(buildDependencySet(INITIAL_NUMBER_DEPENDENCIES, null));

    Map<Dependency, Set<Dependency>> mulePlugins = new TreeMap<>();
    mulePlugins.put(mulePlugin1, mulePlugin1Dependencies);
    mulePlugins.put(mulePlugin2, mulePlugin2Dependencies);

    ClassLoaderModel model = new ClassLoaderModel(VERSION, buildArtifactCoordinates(MULE_APPLICATION_CLASSIFIER));

    model.setMulePlugins(mulePlugins);

    SortedSet<Dependency> additionalMulePlugin2Dependencies =
        new TreeSet<>(buildDependencySet(ADDITIONAL_NUMBER_DEPENDENCIES, null));

    model.addMulePlugin(mulePlugin2, additionalMulePlugin2Dependencies);

    Map<Dependency, Set<Dependency>> actualMulePlugins = model.getMulePlugins();

    assertThat("Number of mule plugins in set is not the expected", actualMulePlugins.keySet().size(),
               equalTo(NUMBER_MULE_PLUGINS));
    assertThat("Mule plugins set is not the expected", actualMulePlugins.keySet(), containsInAnyOrder(mulePlugin1, mulePlugin2));

    assertThat("Number of dependencies of mulePlugin1 is not the expected", actualMulePlugins.get(mulePlugin1).size(),
               equalTo(ARBITRARY_NUMBER_DEPENDENCIES));
    assertThat("Number of dependencies of mulePlugin2 is not the expected", actualMulePlugins.get(mulePlugin2).size(),
               equalTo(INITIAL_NUMBER_DEPENDENCIES + ADDITIONAL_NUMBER_DEPENDENCIES));
  }

  private Dependency buildDependency(String classifier) throws URISyntaxException {
    return new Dependency(buildArtifactCoordinates(classifier), buildURI());
  }

  private Set<Dependency> buildDependencySet(int numberElements, String classifier) throws URISyntaxException {
    Set<Dependency> dependencies = new HashSet<>();
    while (numberElements > 0) {
      ArtifactCoordinates coordinates = buildArtifactCoordinates(classifier);

      URI path = buildURI();

      dependencies.add(new Dependency(coordinates, path));

      numberElements--;
    }
    return dependencies;
  }

  private URI buildURI() throws URISyntaxException {
    return new URI(URI_SEPARATOR + randomAlphabetic(LENGTH).toLowerCase() + URI_SEPARATOR
        + randomAlphabetic(LENGTH).toLowerCase());
  }

  private ArtifactCoordinates buildArtifactCoordinates(String classifier) {
    String groupId = GROUP_ID_PREFIX + randomAlphabetic(LENGTH).toLowerCase();
    String artifactId = randomAlphabetic(LENGTH).toLowerCase() + ARTIFACT_ID_SEPARATOR + randomAlphabetic(LENGTH).toLowerCase();
    String version =
        randomNumeric(NUM_DIGITS) + VERSION_SEPARATOR + randomNumeric(NUM_DIGITS) + VERSION_SEPARATOR + randomNumeric(NUM_DIGITS);
    return new ArtifactCoordinates(groupId, artifactId, version, DEFAULT_ARTIFACT_DESCRIPTOR_TYPE, classifier);
  }

}
