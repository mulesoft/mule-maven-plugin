/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.maven.mojo.model.lifecycle.mapping.version;

import org.mule.tools.maven.mojo.model.lifecycle.mapping.project.ProjectLifecycleMapping;

public class LifecycleMappingMavenFactory {

  public static LifecycleMappingMavenVersionless buildLifecycleMappingMaven(ProjectLifecycleMapping mapping) {
    try {
      loadClass();
      return new LifecycleMappingMaven339OrHigher(mapping);
    } catch (ClassNotFoundException e) {
      return new LifecycleMappingMaven333(mapping);
    }

  }

  protected static void loadClass() throws ClassNotFoundException {
    Class.forName("org.apache.maven.lifecycle.mapping.LifecyclePhase");
  }
}
