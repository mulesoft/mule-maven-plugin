/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.util;

import static org.mule.runtime.api.util.Preconditions.checkArgument;

import java.util.Set;

/**
 * Provides information about packages and resources found in a Java JAR.
 */
public class JarInfo {

  private final Set<String> packages;
  private final Set<String> resources;

  /**
   * Creates a new instance corresponding to a given JAR.
   *
   * @param packages Java packages found on the JAR. Non null.
   * @param resources Java resources found on the JAR. Non null.
   */
  public JarInfo(Set<String> packages, Set<String> resources) {
    checkArgument(packages != null, "Packages cannot be null");
    checkArgument(resources != null, "Resources cannot be null");
    this.packages = packages;
    this.resources = resources;
  }

  /**
   * @return Java packages found on the JAR. Non null.
   */
  public Set<String> getPackages() {
    return packages;
  }

  /**
   * @return Java resources found on the JAR. Non null.
   */
  public Set<String> getResources() {
    return resources;
  }

}
