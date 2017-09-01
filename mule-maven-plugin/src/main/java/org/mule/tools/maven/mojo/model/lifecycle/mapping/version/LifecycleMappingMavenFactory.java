/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
