/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model.anypoint;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.MavenProject;

public class MavenResolverMetadata {

  private ArtifactFactory factory;
  private MavenProject project;
  private ArtifactResolver resolver;
  private ArtifactRepository localRepository;

  public ArtifactFactory getFactory() {
    return factory;
  }

  public MavenProject getProject() {
    return project;
  }

  public ArtifactResolver getResolver() {
    return resolver;
  }

  public ArtifactRepository getLocalRepository() {
    return localRepository;
  }

  public MavenResolverMetadata setFactory(ArtifactFactory factory) {
    this.factory = factory;
    return this;
  }

  public MavenResolverMetadata setProject(MavenProject project) {
    this.project = project;
    return this;
  }

  public MavenResolverMetadata setResolver(ArtifactResolver resolver) {
    this.resolver = resolver;
    return this;
  }

  public MavenResolverMetadata setLocalRepository(ArtifactRepository localRepository) {
    this.localRepository = localRepository;
    return this;
  }
}
