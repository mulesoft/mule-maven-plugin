/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
