/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.resolver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTree;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.maven.repository.MavenProjectBuilder;

public class ExcludedDependenciesResolverTest {

  private static final int NUMBER_NODES_IN_DEPENDENCY_TREE = 4;
  private ExcludedDependenciesResolver resolver;
  private DependencyTreeBuilder treeBuilderMock;
  private ArtifactRepository localRepositoryMock;
  private ArtifactFactory artifactFactoryMock;
  private ArtifactMetadataSource artifactMetadataSourceMock;
  private ArtifactCollector artifactCollectorMock;
  private MavenProjectBuilder mavenProjectBuilderMock;
  private final String EXCLUSION_COORD_A = "org.a:art-a";
  private final String EXCLUSION_COORD_B = "org.b:art-b";
  private final String EXCLUSION_COORD_C = "org.c:art-c";
  private static final String GROUP_ID = "group.id";
  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.";
  private static final String SCOPE = "compile";
  private static final String TYPE = "jar";
  private static final String CLASSIFIER = "classifier";
  private static final String EXCLUSION_COORD = GROUP_ID + "." + 3 + ":" + ARTIFACT_ID + "-" + 3;
  private static final String EXCLUSION_VERSION = VERSION + 3;
  private ArtifactHandler artifactHandler = new DefaultArtifactHandler(TYPE);
  private DependencyTreeBuilder treeBuilder;
  private DependencyNode root;
  private MavenProject projectMock;

  @Before
  public void before() throws DependencyTreeBuilderException {
    treeBuilderMock = mock(DependencyTreeBuilder.class);
    localRepositoryMock = mock(ArtifactRepository.class);
    artifactFactoryMock = mock(ArtifactFactory.class);
    artifactMetadataSourceMock = mock(ArtifactMetadataSource.class);
    artifactCollectorMock = mock(ArtifactCollector.class);
    mavenProjectBuilderMock = mock(MavenProjectBuilder.class);
    treeBuilder = new DependencyTreeBuilderImpl();
    projectMock = mock(MavenProject.class);
    root = treeBuilder.buildDependencyTree(projectMock, localRepositoryMock, artifactFactoryMock, artifactMetadataSourceMock,
                                           null, artifactCollectorMock);
  }

  @Test
  public void shouldStopTraversingTest() {
    resolver = new ExcludedDependenciesResolver(treeBuilderMock, localRepositoryMock, artifactFactoryMock,
                                                artifactMetadataSourceMock, artifactCollectorMock, mavenProjectBuilderMock);
    Set<String> exclusions = new HashSet<>();
    exclusions.add(EXCLUSION_COORD_A);
    exclusions.add(EXCLUSION_COORD_B);
    String exclusionsCoordinates = EXCLUSION_COORD_A;
    boolean shouldStop = resolver.shouldStopTraversing(exclusions, exclusionsCoordinates);
    assertThat("Resolver should stop traversing", shouldStop, is(true));
    shouldStop = resolver.shouldStopTraversing(exclusions, EXCLUSION_COORD_C);
    assertThat("Resolver should stop traversing", shouldStop, is(false));
  }

  @Test
  public void resolveOriginalDependenciesTest() {
    resolver = new ExcludedDependenciesResolver(treeBuilderMock, localRepositoryMock, artifactFactoryMock,
                                                artifactMetadataSourceMock, artifactCollectorMock, mavenProjectBuilderMock);
    List<DependencyNode> nodes = resolver.resolveOriginalDependencies(root);

    List<DependencyNode> expectedNodes = new ArrayList<>();
    expectedNodes.add(root);
    expectedNodes.add((DependencyNode) root.getChildren().get(0));
    expectedNodes.add((DependencyNode) root.getChildren().get(1));
    expectedNodes.add((DependencyNode) ((DependencyNode) root.getChildren().get(1)).getChildren().get(0));

    assertThat("Resolved dependencies cardinality is incorrect", nodes.size(), equalTo(NUMBER_NODES_IN_DEPENDENCY_TREE));
    assertThat("Original dependencies were not successfully resolved", nodes, equalTo(expectedNodes));
  }

  @Test
  public void findExclusionVersionByDepthSearchTest() throws MojoExecutionException {
    resolver = new ExcludedDependenciesResolver(treeBuilderMock, localRepositoryMock, artifactFactoryMock,
                                                artifactMetadataSourceMock, artifactCollectorMock, mavenProjectBuilderMock);
    Set<String> exclusions = new HashSet<>();
    exclusions.add(EXCLUSION_COORD);

    resolver.findExclusionVersionByDepthSearch(root, exclusions);

    assertThat("Excluded artifact was not found in dependency tree", resolver.excludedArtifacts.containsKey(EXCLUSION_COORD),
               is(true));
    assertThat("The cardinality of excluded artifacts is not the expected", resolver.excludedArtifacts.size(), is(1));
    Artifact excludedArtifact = resolver.excludedArtifacts.get(EXCLUSION_COORD);
    assertThat("The resolved version is not the expected", excludedArtifact.getVersion(), is(EXCLUSION_VERSION));
  }

  @Test
  public void resolveExclusionsVersionsTest() throws MojoExecutionException, DependencyTreeBuilderException {
    resolver = spy(new ExcludedDependenciesResolver(treeBuilderMock, localRepositoryMock, artifactFactoryMock,
                                                    artifactMetadataSourceMock, artifactCollectorMock, mavenProjectBuilderMock));

    doNothing().when(resolver).findExclusionsInTree(anyObject(), anyObject());
    resolver.resolveExclusionsVersions(root, new HashSet<>());

    verify(resolver, times(NUMBER_NODES_IN_DEPENDENCY_TREE)).findExclusionsInTree(anyObject(), anyObject());
  }

  private class DependencyTreeBuilderImpl implements DependencyTreeBuilder {

    @Override
    public DependencyTree buildDependencyTree(MavenProject project, ArtifactRepository repository, ArtifactFactory factory,
                                              ArtifactMetadataSource metadataSource, ArtifactCollector collector)
        throws DependencyTreeBuilderException {

      DependencyNode root = new DependencyNode(createArtifact(0));
      DependencyNode child1 = new DependencyNode(createArtifact(1));
      DependencyNode child2 = new DependencyNode(createArtifact(2));
      DependencyNode child3 = new DependencyNode(createArtifact(3));

      root.addChild(child1);
      root.addChild(child2);
      child2.addChild(child3);

      Set<DependencyNode> nodes = new HashSet<>();
      nodes.add(root);
      nodes.add(child1);
      nodes.add(child2);

      return new DependencyTree(root, nodes);
    }

    @Override
    public DependencyNode buildDependencyTree(MavenProject project, ArtifactRepository repository, ArtifactFactory factory,
                                              ArtifactMetadataSource metadataSource, ArtifactFilter filter,
                                              ArtifactCollector collector)
        throws DependencyTreeBuilderException {
      return buildDependencyTree(project, repository, factory, metadataSource, collector).getRootNode();
    }
  }

  private Artifact createArtifact(int i) {
    return new DefaultArtifact(GROUP_ID + "." + i, ARTIFACT_ID + "-" + i, VERSION + i, SCOPE, TYPE,
                               CLASSIFIER, artifactHandler);
  }
}
