/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.resources.generator;

import org.apache.maven.execution.MavenSession;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.mule.maven.client.api.MavenClient;
import org.mule.maven.pom.parser.api.model.BundleDependency;
import org.mule.maven.pom.parser.api.model.BundleDescriptor;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.packager.resources.content.ResourcesContent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DomainBundleProjectResourcesContentGeneratorTest {


  @Test
  public void domainContentGeneratorShouldWork() throws URISyntaxException {
    MavenClient mavenClient = mock(MavenClient.class);
    ArtifactCoordinates coordinates = new ArtifactCoordinates("org.mule.tools.maven", "mule-classloader-model", "4.1.0");
    List<ArtifactCoordinates> projectDependencies = new ArrayList<>();
    projectDependencies.add(coordinates);
    BundleDependency.Builder dependencyBuilder = new BundleDependency.Builder();
    BundleDescriptor.Builder descriptorBuilder = new BundleDescriptor.Builder();
    descriptorBuilder.setGroupId("group");
    descriptorBuilder.setArtifactId("artifact");
    descriptorBuilder.setBaseVersion("1.1.1");
    descriptorBuilder.setVersion("1.1.1");
    dependencyBuilder.setBundleDescriptor(descriptorBuilder.build());
    dependencyBuilder.setBundleUri(new URI("uri"));
    when(mavenClient.resolveBundleDescriptor(any())).thenReturn(dependencyBuilder.build());
    ResourcesContent content = (new DomainBundleProjectResourcesContentGenerator(mavenClient, projectDependencies).generate());
    content.getResources();
  }
}
