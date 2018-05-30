/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo.model.lifecycle.mapping.project;

import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenVersionless;

import java.util.Map;

public interface ProjectLifecycleMapping {

  <V> Map<String, V> getLifecyclePhases(LifecycleMappingMavenVersionless mapping);
}
