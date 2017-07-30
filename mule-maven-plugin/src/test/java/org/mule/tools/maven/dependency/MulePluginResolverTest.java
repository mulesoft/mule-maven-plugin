/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.dependency;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.maven.utils.MavenProjectBuilder;

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
  private MulePluginResolver resolver;
  private MavenProjectBuilder mavenProjectBuilderMock;
  private Dependency dependency;
  private MavenProject mavenProjectMock;
  private List<Dependency> dependencies;

  @Before
  public void before() {
    mavenProjectBuilderMock = mock(MavenProjectBuilder.class);
    mavenProjectMock = mock(MavenProject.class);
    resolver = new MulePluginResolver(mavenProjectBuilderMock);
    dependency = new Dependency();
    dependency.setType(MULE_PLUGIN_TYPE);
    dependency.setScope(COMPILE_SCOPE);
    dependency.setClassifier(MULE_PLUGIN_CLASSIFIER);
    dependencies = new ArrayList<>();
  }

  @Test
  public void resolveMulePluginsTest() throws MojoExecutionException {
    Map<Dependency, List<Dependency>> projectStructure = createMainResolvableProjectDependencyTree();
    setUpProjectBuilderMock(projectStructure);
    when(mavenProjectMock.getDependencies()).thenReturn(projectStructure.get(dependency));
    List<Dependency> actualResolvedMulePlugins = resolver.resolveMulePlugins(mavenProjectMock);
    assertThat("Number of resolved mule plugins is not the expected", actualResolvedMulePlugins.size(), equalTo(5));
    assertThat("Not all resolved dependencies are mule plugins", actualResolvedMulePlugins.stream()
        .allMatch(dependency -> dependency.getClassifier().equals(MULE_PLUGIN_CLASSIFIER)), is(true));
  }

  @Test
  public void resolveMulePluginsOfScopeProjectWithoutDependenciesTest() {
    when(mavenProjectMock.getDependencies()).thenReturn(Collections.emptyList());
    assertThat("Result should be empty", resolver.resolveMulePluginsOfScope(mavenProjectMock, COMPILE_SCOPE).isEmpty(), is(true));
  }

  @Test
  public void resolveMulePluginsOfScopeProjectWithoutMulePluginsAsDependenciesTest() {
    IntStream.range(0, 10)
        .forEach(i -> dependencies.add(createDependency(i, "1.0.0", COMPILE_SCOPE, NOT_MULE_PLUGIN_CLASSIFIER)));
    when(mavenProjectMock.getDependencies()).thenReturn(dependencies);
    assertThat("Result should be empty", resolver.resolveMulePluginsOfScope(mavenProjectMock, COMPILE_SCOPE).isEmpty(), is(true));
  }

  @Test
  public void resolveMulePluginsOfScopeProjectWithSomeMulePluginsAsDependenciesTest() {
    IntStream.range(0, 10)
        .forEach(i -> dependencies.add(createDependency(i, "1.0.0", COMPILE_SCOPE, NOT_MULE_PLUGIN_CLASSIFIER)));
    IntStream.range(0, 5).forEach(i -> dependencies.add(createDependency(i, "1.0.0", COMPILE_SCOPE, MULE_PLUGIN_CLASSIFIER)));
    when(mavenProjectMock.getDependencies()).thenReturn(dependencies);
    assertThat("Result should contain 5 elements", resolver.resolveMulePluginsOfScope(mavenProjectMock, COMPILE_SCOPE).size(),
               equalTo(5));
  }

  @Test
  public void resolveMulePluginsOfScopeProjectWithJustMulePluginsAsDependenciesTest() {
    IntStream.range(0, 5).forEach(i -> dependencies.add(createDependency(i, "1.0.0", COMPILE_SCOPE, MULE_PLUGIN_CLASSIFIER)));
    when(mavenProjectMock.getDependencies()).thenReturn(dependencies);
    assertThat("Result should contain 5 elements", resolver.resolveMulePluginsOfScope(mavenProjectMock, COMPILE_SCOPE).size(),
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

  private Dependency createDependency(int i, String version, String scope, String classifier) {
    Dependency dependency = new Dependency();
    dependency.setGroupId("group.id." + i);
    dependency.setArtifactId("artifact-id-" + i);
    dependency.setType("jar");
    dependency.setVersion(version);
    dependency.setScope(scope);
    dependency.setClassifier(classifier);
    return dependency;
  }

  private void setUpProjectBuilderMock(Map<Dependency, List<Dependency>> projectStructure) throws MojoExecutionException {
    for (Map.Entry entry : projectStructure.entrySet()) {
      MavenProject projectMock = mock(MavenProject.class);
      when(projectMock.getDependencies()).thenReturn((List<Dependency>) entry.getValue());
      when(mavenProjectBuilderMock.buildMavenProject((Dependency) entry.getKey())).thenReturn(projectMock);
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
  private Map<Dependency, List<Dependency>> createMainResolvableProjectDependencyTree() {
    dependency = createDependency(0, "1.0.0", COMPILE_SCOPE, NOT_MULE_PLUGIN_CLASSIFIER);
    Dependency dependency1 = createDependency(1, "1.0.0", COMPILE_SCOPE, MULE_PLUGIN_CLASSIFIER);
    Dependency dependency2 = createDependency(2, "1.0.0", PROVIDED_SCOPE, MULE_PLUGIN_CLASSIFIER);
    Dependency dependency3 = createDependency(3, "1.0.0", COMPILE_SCOPE, NOT_MULE_PLUGIN_CLASSIFIER);
    Dependency dependency4 = createDependency(4, "1.0.0", COMPILE_SCOPE, MULE_PLUGIN_CLASSIFIER);
    Dependency dependency5 = createDependency(5, "1.0.0", PROVIDED_SCOPE, MULE_PLUGIN_CLASSIFIER);
    Dependency dependency6 = createDependency(6, "1.0.0", PROVIDED_SCOPE, NOT_MULE_PLUGIN_CLASSIFIER);
    Dependency dependency7 = createDependency(7, "1.0.0", PROVIDED_SCOPE, MULE_PLUGIN_CLASSIFIER);
    Dependency dependency7OtherVersion = createDependency(7, "1.2.1", PROVIDED_SCOPE, MULE_PLUGIN_CLASSIFIER);

    Map<Dependency, List<Dependency>> projectStructure = new HashMap<>();

    List<Dependency> directDependencies = new ArrayList<>();
    directDependencies.add(dependency1);
    directDependencies.add(dependency2);
    directDependencies.add(dependency3);
    directDependencies.add(dependency4);
    projectStructure.put(dependency, directDependencies);

    List<Dependency> transitiveDependency1Dependencies = new ArrayList<>();
    transitiveDependency1Dependencies.add(dependency5);
    transitiveDependency1Dependencies.add(dependency6);
    projectStructure.put(dependency1, transitiveDependency1Dependencies);

    List<Dependency> transitiveDependency4Dependencies = new ArrayList<>();
    transitiveDependency4Dependencies.add(dependency7OtherVersion);
    projectStructure.put(dependency4, transitiveDependency4Dependencies);

    List<Dependency> transitiveDependency5Dependencies = new ArrayList<>();
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
