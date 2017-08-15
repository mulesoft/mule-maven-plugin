/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.validation;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ProjectBuildingException;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.util.ProjectBuilder;

import java.util.*;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MulePluginResolverTest {

  private static final String MULE_PLUGIN_TYPE = "jar";
  private static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  private static final String COMPILE_SCOPE = "compile";
  private static final String PROVIDED_SCOPE = "provided";
  private static final String NOT_MULE_PLUGIN_CLASSIFIER = "not-mule-plugin";
  private static final String NOT_MULE_PLUGIN_TYPE = "zip";
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private MulePluginResolver resolver;
  private ProjectBuilder projectBuilderMock;
  private ArtifactCoordinates dependency;
  private Project projectMock;
  private List<ArtifactCoordinates> dependencies;

  @Before
  public void before() {
    projectBuilderMock = mock(ProjectBuilder.class);
    projectMock = mock(Project.class);
    resolver = new MulePluginResolver(projectBuilderMock);
    dependency = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, MULE_PLUGIN_TYPE, MULE_PLUGIN_CLASSIFIER, COMPILE_SCOPE);
    dependencies = new ArrayList<>();
  }

  @Test
  public void resolveMulePluginsTest() throws ProjectBuildingException, ValidationException {
    Map<ArtifactCoordinates, List<ArtifactCoordinates>> projectStructure = createMainResolvableProjectDependencyTree();
    setUpProjectBuilderMock(projectStructure);
    when(projectMock.getDependencies()).thenReturn(projectStructure.get(dependency));
    List<ArtifactCoordinates> actualResolvedMulePlugins = resolver.resolveMulePlugins(projectMock);
    assertThat("Number of resolved mule plugins is not the expected", actualResolvedMulePlugins.size(), equalTo(5));
    assertThat("Not all resolved dependencies are mule plugins", actualResolvedMulePlugins.stream()
        .allMatch(dependency -> dependency.getClassifier().equals(MULE_PLUGIN_CLASSIFIER)), is(true));
  }

  @Test
  public void resolveMulePluginsOfScopeProjectWithoutDependenciesTest() {
    when(projectMock.getDependencies()).thenReturn(Collections.emptyList());
    assertThat("Result should be empty", resolver.resolveMulePluginsOfScope(projectMock, COMPILE_SCOPE).isEmpty(), is(true));
  }

  @Test
  public void resolveMulePluginsOfScopeProjectWithoutMulePluginsAsDependenciesTest() {
    IntStream.range(0, 10)
        .forEach(i -> dependencies.add(createDependency(i, "1.0.0", COMPILE_SCOPE, NOT_MULE_PLUGIN_CLASSIFIER)));
    when(projectMock.getDependencies()).thenReturn(dependencies);
    assertThat("Result should be empty", resolver.resolveMulePluginsOfScope(projectMock, COMPILE_SCOPE).isEmpty(), is(true));
  }

  @Test
  public void resolveMulePluginsOfScopeProjectWithSomeMulePluginsAsDependenciesTest() {
    IntStream.range(0, 10)
        .forEach(i -> dependencies.add(createDependency(i, "1.0.0", COMPILE_SCOPE, NOT_MULE_PLUGIN_CLASSIFIER)));
    IntStream.range(0, 5).forEach(i -> dependencies.add(createDependency(i, "1.0.0", COMPILE_SCOPE, MULE_PLUGIN_CLASSIFIER)));
    when(projectMock.getDependencies()).thenReturn(dependencies);
    assertThat("Result should contain 5 elements", resolver.resolveMulePluginsOfScope(projectMock, COMPILE_SCOPE).size(),
               equalTo(5));
  }

  @Test
  public void resolveMulePluginsOfScopeProjectWithJustMulePluginsAsDependenciesTest() {
    IntStream.range(0, 5).forEach(i -> dependencies.add(createDependency(i, "1.0.0", COMPILE_SCOPE, MULE_PLUGIN_CLASSIFIER)));
    when(projectMock.getDependencies()).thenReturn(dependencies);
    assertThat("Result should contain 5 elements", resolver.resolveMulePluginsOfScope(projectMock, COMPILE_SCOPE).size(),
               equalTo(5));
  }

  @Test
  public void dependencyWithExpectedScopeTest() {
    assertThat("Predicate should return true", resolver.dependencyWith(COMPILE_SCOPE).test(dependency), is(true));
  }

  @Test
  public void dependencyWithWrongScopeTest() {
    assertThat("Predicate should return false", resolver.dependencyWith(PROVIDED_SCOPE).test(dependency), is(false));
  }

  @Test
  public void dependencyWithWrongClassifierTest() {
    dependency.setClassifier(NOT_MULE_PLUGIN_CLASSIFIER);
    assertThat("Predicate should return false", resolver.dependencyWith(COMPILE_SCOPE).test(dependency), is(false));
  }

  @Test
  public void dependencyWithEmptyClassifierTest() {
    dependency.setClassifier(StringUtils.EMPTY);
    assertThat("Predicate should return false", resolver.dependencyWith(COMPILE_SCOPE).test(dependency), is(false));
  }

  @Test
  public void dependencyWithNullClassifierTest() {
    dependency.setClassifier(null);
    assertThat("Predicate should return false", resolver.dependencyWith(COMPILE_SCOPE).test(dependency), is(false));
  }

  @Test
  public void dependencyWithWrongTypeTest() {
    dependency.setType(NOT_MULE_PLUGIN_TYPE);
    assertThat("Predicate should return false", resolver.dependencyWith(COMPILE_SCOPE).test(dependency), is(false));
  }

  @Test
  public void dependencyWithEmptyScopeArgumentTest() {
    assertThat("Predicate should return false", resolver.dependencyWith(StringUtils.EMPTY).test(dependency), is(false));
  }

  @Test
  public void dependencyWithNullArgumentTest() {
    assertThat("Predicate should return false", resolver.dependencyWith(null).test(dependency), is(false));
  }

  private ArtifactCoordinates createDependency(int i, String version, String scope, String classifier) {
    return new ArtifactCoordinates(GROUP_ID + "." + i, ARTIFACT_ID + "-" + i, version, "jar", classifier, scope);
  }

  private void setUpProjectBuilderMock(Map<ArtifactCoordinates, List<ArtifactCoordinates>> projectStructure)
      throws ProjectBuildingException {
    for (Map.Entry entry : projectStructure.entrySet()) {
      Project projectMock = mock(Project.class);
      when(projectMock.getDependencies()).thenReturn((List<ArtifactCoordinates>) entry.getValue());
      when(projectBuilderMock.buildProject((ArtifactCoordinates) entry.getKey())).thenReturn(projectMock);
    }
  }

  /**
   * Project structure
   * <p>
   * 
   * <pre>
   *            0
   *        ____|____
   *       /  |   |  \
   *      1   2   3   4
   *     _|_          |
   *    /   \         7'
   *    5   6
   *    |
   *    7
   *
   * 0 -> Project root
   * 1, 4 -> Mule plugins, compile scope
   * 2 -> Not a mule plugin, compile scope
   * 3, 5, 7, 7' -> Mule plugin, provided scope
   * 6 -> Not mule plugin, provided scope
   * 7 and 7' are version compatible.
   * </pre>
   */
  private Map<ArtifactCoordinates, List<ArtifactCoordinates>> createMainResolvableProjectDependencyTree() {
    dependency = createDependency(0, "1.0.0", COMPILE_SCOPE, NOT_MULE_PLUGIN_CLASSIFIER);
    ArtifactCoordinates dependency1 = createDependency(1, "1.0.0", COMPILE_SCOPE, MULE_PLUGIN_CLASSIFIER);
    ArtifactCoordinates dependency2 = createDependency(2, "1.0.0", PROVIDED_SCOPE, MULE_PLUGIN_CLASSIFIER);
    ArtifactCoordinates dependency3 = createDependency(3, "1.0.0", COMPILE_SCOPE, NOT_MULE_PLUGIN_CLASSIFIER);
    ArtifactCoordinates dependency4 = createDependency(4, "1.0.0", COMPILE_SCOPE, MULE_PLUGIN_CLASSIFIER);
    ArtifactCoordinates dependency5 = createDependency(5, "1.0.0", PROVIDED_SCOPE, MULE_PLUGIN_CLASSIFIER);
    ArtifactCoordinates dependency6 = createDependency(6, "1.0.0", PROVIDED_SCOPE, NOT_MULE_PLUGIN_CLASSIFIER);
    ArtifactCoordinates dependency7 = createDependency(7, "1.0.0", PROVIDED_SCOPE, MULE_PLUGIN_CLASSIFIER);
    ArtifactCoordinates dependency7OtherVersion = createDependency(7, "1.2.1", PROVIDED_SCOPE, MULE_PLUGIN_CLASSIFIER);

    Map<ArtifactCoordinates, List<ArtifactCoordinates>> projectStructure = new HashMap<>();

    List<ArtifactCoordinates> directDependencies = new ArrayList<>();
    directDependencies.add(dependency1);
    directDependencies.add(dependency2);
    directDependencies.add(dependency3);
    directDependencies.add(dependency4);
    projectStructure.put(dependency, directDependencies);

    List<ArtifactCoordinates> transitiveDependency1Dependencies = new ArrayList<>();
    transitiveDependency1Dependencies.add(dependency5);
    transitiveDependency1Dependencies.add(dependency6);
    projectStructure.put(dependency1, transitiveDependency1Dependencies);

    List<ArtifactCoordinates> transitiveDependency4Dependencies = new ArrayList<>();
    transitiveDependency4Dependencies.add(dependency7OtherVersion);
    projectStructure.put(dependency4, transitiveDependency4Dependencies);

    List<ArtifactCoordinates> transitiveDependency5Dependencies = new ArrayList<>();
    transitiveDependency5Dependencies.add(dependency7);
    projectStructure.put(dependency5, transitiveDependency5Dependencies);

    projectStructure.put(dependency2, Collections.emptyList());
    projectStructure.put(dependency3, Collections.emptyList());
    projectStructure.put(dependency6, Collections.emptyList());
    projectStructure.put(dependency7, Collections.emptyList());
    projectStructure.put(dependency7OtherVersion, Collections.emptyList());

    return projectStructure;
  }
}
