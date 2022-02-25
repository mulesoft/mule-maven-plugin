/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package util;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ProjectBuildingException;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.util.ProjectBuilder;
import org.mule.tools.api.validation.resolver.model.ProjectDependencyNode;
import org.mule.tools.api.validation.resolver.visitor.AbstractArtifactVisitor;
import org.mule.tools.api.validation.resolver.visitor.DependencyNodeVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class ResolverTestHelper {

  public static final String CLASSIFIER_A = "classifier-a";
  public static final String CLASSIFIER_B = "classifier-b";
  public static final String SCOPE_A = "scope-1";
  public static final String SCOPE_B = "scope-2";
  public static final String TYPE_A = "type_a";
  public static final String TYPE_B = "type_b";
  public static final String GROUP_ID = "group.id";
  public static final String ARTIFACT_ID_A = "artifact-id-a";
  public static final String ARTIFACT_ID_B = "artifact-id-b";
  public static final String COMPILE_SCOPE = "compile";
  public static final String PROVIDED_SCOPE = "provided";
  public static final String NOT_MULE_PLUGIN_CLASSIFIER = "not-mule-plugin";
  public static final String GROUP_ID_PREFIX = "group.id.";
  public static final String ARTIFACT_ID_PREFIX = "artifact-id-";
  public static final String VERSION = "1.0.0";
  public static final String JAR_TYPE = "jar";
  public static final String MULE_PLUGIN_CLASSIFIER = "mule-plugin";
  public static final String SCOPE = "provided";

  public static AbstractArtifactVisitor getChildVisitorMock(Set<ArtifactCoordinates> dependenciesCollected) {
    AbstractArtifactVisitor childVisitorMock = mock(AbstractArtifactVisitor.class);
    when(childVisitorMock.getCollectedDependencies()).thenReturn(dependenciesCollected);
    return childVisitorMock;
  }

  public static Set<ProjectDependencyNode> buildProjectDependencyNodeSpies(int numberDependencies) throws ValidationException {
    return newHashSet(buildGavToDependenciesMap(numberDependencies, StringUtils.EMPTY).values());
  }

  public static Set<ArtifactCoordinates> buildDependencies(int numberDependencies) throws ValidationException {
    return buildGavToDependenciesMap(numberDependencies, StringUtils.EMPTY).keySet();
  }

  public static Map<ArtifactCoordinates, ProjectDependencyNode> buildGavToDependenciesMap(int numberDependencies,
                                                                                          String classifier)
      throws ValidationException {
    Map<ArtifactCoordinates, ProjectDependencyNode> dependencies = new HashMap<>();
    for (int i = 0; i < numberDependencies; ++i) {
      ArtifactCoordinates artifactCoordinates = createJarDependency(i, VERSION, classifier, SCOPE);
      ProjectDependencyNode node = createProjectDependencyNodeSpy();
      dependencies.put(artifactCoordinates, node);
    }
    return dependencies;
  }

  public static ProjectDependencyNode createProjectDependencyNodeSpy() throws ValidationException {
    ProjectDependencyNode nodeSpy = spy(new ProjectDependencyNode(mock(Project.class), mock(ProjectBuilder.class)));
    doNothing().when(nodeSpy).accept(any());
    return nodeSpy;
  }

  public static List<DependencyNodeVisitor> buildVisitorSpies(ProjectDependencyNode nodeSpy,
                                                              Set<ArtifactCoordinates> collectedDependencies, int numberVisitors)
      throws ValidationException {
    List<DependencyNodeVisitor> visitorSpies = new ArrayList<>();
    for (int i = 0; i < numberVisitors; ++i) {
      DependencyNodeVisitor visitorSpy = spy(DependencyNodeVisitor.class);
      doNothing().when(visitorSpy).visit(nodeSpy);
      ArtifactCoordinates dependency = createJarDependency(i);
      collectedDependencies.add(dependency);
      doReturn(newHashSet(dependency)).when(visitorSpy).getCollectedDependencies();
      visitorSpies.add(visitorSpy);
    }
    return visitorSpies;
  }

  public static ArtifactCoordinates createDependencyWithClassifierAndScope(String classifier, String scope) {
    return createJarDependency(0, VERSION, classifier, scope);
  }

  public static ArtifactCoordinates createDependency(String groupId, String artifactId, String version, String type,
                                                     String classifier, String scope) {
    return new ArtifactCoordinates(groupId, artifactId, version, type, classifier, scope);
  }

  public static ArtifactCoordinates createDependency(int i, String version, String type, String classifier, String scope) {
    return createDependency(GROUP_ID_PREFIX + "." + i, ARTIFACT_ID_PREFIX + "-" + i, version, type, classifier, scope);
  }

  public static ArtifactCoordinates createJarDependency(int i, String version, String classifier, String scope) {
    return createDependency(GROUP_ID_PREFIX + "." + i, ARTIFACT_ID_PREFIX + "-" + i, version, JAR_TYPE, classifier, scope);
  }

  private static ArtifactCoordinates createJarDependency(int i) {
    return new ArtifactCoordinates(GROUP_ID_PREFIX + "." + i, ARTIFACT_ID_PREFIX + "-" + i, VERSION);
  }

  public static void stubBuildNodeMethod(ProjectDependencyNode nodeSpy,
                                         Map<ArtifactCoordinates, ProjectDependencyNode> dependenciesMock) {
    dependenciesMock.keySet().forEach(dep -> {
      try {
        doReturn(dependenciesMock.get(dep)).when(nodeSpy).buildNode(dep);
      } catch (ValidationException e) {
        e.printStackTrace();
      }
    });
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
  public static Map<ArtifactCoordinates, List<ArtifactCoordinates>> createMainResolvableProjectDependencyTree(ArtifactCoordinates dependency) {
    dependency = createJarDependency(0, "1.0.0", NOT_MULE_PLUGIN_CLASSIFIER, COMPILE_SCOPE);
    ArtifactCoordinates dependency1 = createJarDependency(1, "1.0.0", MULE_PLUGIN_CLASSIFIER, COMPILE_SCOPE);
    ArtifactCoordinates dependency2 = createJarDependency(2, "1.0.0", MULE_PLUGIN_CLASSIFIER, PROVIDED_SCOPE);
    ArtifactCoordinates dependency3 = createJarDependency(3, "1.0.0", NOT_MULE_PLUGIN_CLASSIFIER, COMPILE_SCOPE);
    ArtifactCoordinates dependency4 = createJarDependency(4, "1.0.0", MULE_PLUGIN_CLASSIFIER, COMPILE_SCOPE);
    ArtifactCoordinates dependency5 = createJarDependency(5, "1.0.0", MULE_PLUGIN_CLASSIFIER, PROVIDED_SCOPE);
    ArtifactCoordinates dependency6 = createJarDependency(6, "1.0.0", NOT_MULE_PLUGIN_CLASSIFIER, PROVIDED_SCOPE);
    ArtifactCoordinates dependency7 = createJarDependency(7, "1.0.0", MULE_PLUGIN_CLASSIFIER, PROVIDED_SCOPE);
    ArtifactCoordinates dependency7OtherVersion = createJarDependency(7, "1.2.1", MULE_PLUGIN_CLASSIFIER, PROVIDED_SCOPE);

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

    projectStructure.put(dependency2, emptyList());
    projectStructure.put(dependency3, emptyList());
    projectStructure.put(dependency6, emptyList());
    projectStructure.put(dependency7, emptyList());
    projectStructure.put(dependency7OtherVersion, emptyList());

    return projectStructure;
  }

  public static void setUpProjectBuilderMock(Map<ArtifactCoordinates, List<ArtifactCoordinates>> projectStructure,
                                             ProjectBuilder projectBuilderMock)
      throws ProjectBuildingException {
    for (Map.Entry entry : projectStructure.entrySet()) {
      Project projectMock = mock(Project.class);
      when(projectMock.getDirectDependencies()).thenReturn((List<ArtifactCoordinates>) entry.getValue());
      when(projectBuilderMock.buildProject((ArtifactCoordinates) entry.getKey())).thenReturn(projectMock);
    }
  }
}
