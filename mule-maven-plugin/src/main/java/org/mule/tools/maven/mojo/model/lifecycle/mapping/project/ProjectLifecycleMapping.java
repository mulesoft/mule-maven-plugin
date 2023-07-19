/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.maven.mojo.model.lifecycle.mapping.project;

import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenVersionless;

import java.util.Map;

public interface ProjectLifecycleMapping {

  <V> Map<String, V> getLifecyclePhases(LifecycleMappingMavenVersionless mapping);
}
