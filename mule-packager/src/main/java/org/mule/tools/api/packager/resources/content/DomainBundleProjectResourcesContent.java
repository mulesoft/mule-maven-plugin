/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.packager.resources.content;

import org.mule.tools.api.classloader.model.Artifact;

import java.util.*;

/**
 * Resources present in a domain bundle, namely, applications and a domain jar files.
 */
public class DomainBundleProjectResourcesContent implements ResourcesContent {

  private final List<Artifact> dependencies = new ArrayList<>();

  @Override
  public List<Artifact> getResources() {
    return dependencies;
  }

  @Override
  public void add(Artifact resource) {
    dependencies.add(resource);
  }
}
