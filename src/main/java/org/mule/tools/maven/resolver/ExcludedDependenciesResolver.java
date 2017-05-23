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

import java.util.*;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.apache.maven.shared.dependency.tree.traversal.CollectingDependencyNodeVisitor;
import org.mule.tools.maven.repository.MavenProjectBuilder;

public class ExcludedDependenciesResolver {

  private final MavenProjectBuilder mavenProjectBuilder;
  private final DependencyTreeBuilder treeBuilder;
  private final ArtifactRepository localRepository;
  private final ArtifactFactory artifactFactory;
  private final ArtifactMetadataSource artifactMetadataSource;
  private final ArtifactCollector artifactCollector;
  protected final Map<String, Artifact> excludedArtifacts;
  private final ArtifactFilter artifactFilter;

  public ExcludedDependenciesResolver(DependencyTreeBuilder treeBuilder, ArtifactRepository localRepository,
                                      ArtifactFactory artifactFactory, ArtifactMetadataSource artifactMetadataSource,
                                      ArtifactCollector artifactCollector, MavenProjectBuilder mavenProjectBuilder) {
    this.treeBuilder = treeBuilder;
    this.localRepository = localRepository;
    this.artifactFactory = artifactFactory;
    this.artifactMetadataSource = artifactMetadataSource;
    this.artifactCollector = artifactCollector;
    this.mavenProjectBuilder = mavenProjectBuilder;
    this.excludedArtifacts = new HashMap<>();
    this.artifactFilter = new ScopeArtifactFilter(null);
  }

  public Set<Artifact> resolve(MavenProject project, Set<String> exclusions)
      throws MojoExecutionException, DependencyTreeBuilderException {
    DependencyNode rootNode = getProjectTreeRoot(project);
    resolveExclusionsVersions(rootNode, exclusions);
    return new HashSet<>(excludedArtifacts.values());
  }

  public void resolveExclusionsVersions(DependencyNode root, Set<String> exclusions)
      throws DependencyTreeBuilderException, MojoExecutionException {
    List<DependencyNode> originalDependenciesNodes = resolveOriginalDependencies(root);
    for (DependencyNode dependencyNode : originalDependenciesNodes) {
      findExclusionsInTree(dependencyNode, exclusions);
    }
  }

  protected List<DependencyNode> resolveOriginalDependencies(DependencyNode root) {
    CollectingDependencyNodeVisitor visitor = new CollectingDependencyNodeVisitor();
    root.accept(visitor);
    return visitor.getNodes();
  }

  protected void findExclusionsInTree(DependencyNode dependencyNode, Set<String> exclusions)
      throws MojoExecutionException, DependencyTreeBuilderException {
    DefaultArtifact artifact = (DefaultArtifact) dependencyNode.getArtifact();
    artifact.setDependencyFilter(new EmptyFilter());
    MavenProject project = mavenProjectBuilder.buildProjectFromArtifact(artifact);
    DependencyNode resolvedRoot = getProjectTreeRoot(project);
    findExclusionVersionByDepthSearch(resolvedRoot, exclusions);
  }

  protected void findExclusionVersionByDepthSearch(DependencyNode node, Set<String> exclusions) throws MojoExecutionException {
    DefaultArtifact currentArtifact = (DefaultArtifact) node.getArtifact();
    String exclusionCoordinates = currentArtifact.getGroupId() + ":" + currentArtifact.getArtifactId();

    if (shouldStopTraversing(exclusions, exclusionCoordinates)) {
      excludedArtifacts.put(exclusionCoordinates, excludedArtifacts.getOrDefault(exclusionCoordinates, currentArtifact));
      return;
    }

    for (Object child : node.getChildren()) {
      findExclusionVersionByDepthSearch((DependencyNode) child, exclusions);
    }
  }

  protected boolean shouldStopTraversing(Set<String> exclusions, String exclusionCoordinates) {
    return exclusions.contains(exclusionCoordinates);
  }

  private DependencyNode getProjectTreeRoot(MavenProject project) throws DependencyTreeBuilderException {
    return treeBuilder.buildDependencyTree(project, localRepository, artifactFactory, artifactMetadataSource,
                                           artifactFilter, artifactCollector);
  }

  private class EmptyFilter implements ArtifactFilter {

    @Override
    public boolean include(Artifact artifact) {
      return true;
    }
  }
}
