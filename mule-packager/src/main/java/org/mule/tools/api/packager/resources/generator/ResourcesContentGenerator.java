/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.resources.generator;

import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.packager.resources.content.ResourcesContent;


public interface ResourcesContentGenerator {

  default BundleDescriptor toBundleDescriptor(ArtifactCoordinates artifactCoordinates) {
    return new BundleDescriptor.Builder()
        .setGroupId(artifactCoordinates.getGroupId())
        .setArtifactId(artifactCoordinates.getArtifactId())
        .setVersion(artifactCoordinates.getVersion())
        .setBaseVersion(artifactCoordinates.getVersion())
        .setClassifier(artifactCoordinates.getClassifier())
        .setType(artifactCoordinates.getType()).build();
  }

  ResourcesContent generate();
}
