/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo.model.lifecycle;

import java.util.List;
import java.util.Map;

import org.apache.maven.lifecycle.mapping.LifecycleMapping;

public class MuleLifecycleMapping implements LifecycleMapping {

  private MuleLifecycleMappingMaven muleLifecycleMappingMaven;

  @Override
  public List<String> getOptionalMojos(String lifecycle) {
    return null;
  }

  @Override
  public Map<String, String> getPhases(String lifecycle) {
    return null;
  }

  @Override
  public Map getLifecycles() {
    Map lifecycles;
    try {
      Class.forName("org.apache.maven.lifecycle.mapping.LifecyclePhase");
      muleLifecycleMappingMaven = new MuleLifecycleMappingMaven339();
      lifecycles = muleLifecycleMappingMaven.getLifecycles();
    } catch (ClassNotFoundException | ClassCastException e) {
      muleLifecycleMappingMaven = new MuleLifecycleMappingMaven333();
      lifecycles = muleLifecycleMappingMaven.getLifecycles();
    }
    return lifecycles;
  }
}
