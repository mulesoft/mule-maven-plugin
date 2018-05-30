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

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.lifecycle.mapping.Lifecycle;

/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

public abstract class LifecycleMappingMavenVersionless {

  private static final String DEFAULT_LIFECYCLE_ID = "default";

  public Map<String, Lifecycle> getLifecycles() {
    Lifecycle lifecycle = getDefaultLifecycle();
    Map<String, Lifecycle> lifecycleMap = new HashMap<>();
    lifecycleMap.put(lifecycle.getId(), lifecycle);
    return lifecycleMap;
  }

  protected Lifecycle getDefaultLifecycle() {
    Lifecycle lifecycle = new Lifecycle();
    lifecycle.setId(DEFAULT_LIFECYCLE_ID);
    setLifecyclePhases(lifecycle);
    return lifecycle;
  }

  public abstract <T> T buildGoals(String goals);

  public abstract void setLifecyclePhases(Lifecycle lifecycle);

}
