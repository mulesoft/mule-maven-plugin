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

import org.apache.maven.lifecycle.mapping.Lifecycle;
import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMaven333;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenFactory;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenVersionless;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.*;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.DEPLOY;

public class DomainBundleLifecycleMapping implements LifecycleMapping, ProjectLifecycleMapping {

  private static final String MULE_DEPLOY = "muleDeploy";

  @Override
  public List<String> getOptionalMojos(String lifecycle) {
    return null;
  }

  @Override
  public Map<String, String> getPhases(String lifecycle) {
    return getLifecyclePhases(new LifecycleMappingMaven333(this));
  }

  @Override
  public Map<String, Lifecycle> getLifecycles() {
    // This method implementation is to save issues between Maven versions 3.3.3/3.3./3.5.0
    LifecycleMappingMavenVersionless mapping = LifecycleMappingMavenFactory.buildLifecycleMappingMaven(this);
    return mapping.getLifecycles();
  }

  @Override
  public <V> Map<String, V> getLifecyclePhases(LifecycleMappingMavenVersionless mapping) {
    Map<String, V> phases = new HashMap<>();
    phases.put(VALIDATE.id(), mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:validate"));
    phases.put(INITIALIZE.id(), mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:initialize"));
    phases.put(GENERATE_RESOURCES.id(), mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:generate-resources"));
    phases.put(PROCESS_RESOURCES.id(), mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:process-resources"));
    phases.put(GENERATE_SOURCES.id(), mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:generate-sources"));
    phases.put(PACKAGE.id(), mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:package"));
    phases.put(VERIFY.id(), mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:verify"));
    phases.put(INSTALL.id(), mapping.buildGoals("org.apache.maven.plugins:maven-install-plugin:2.5.2:install"));

    String isMuleDeploy = System.getProperty(MULE_DEPLOY);
    if (isMuleDeploy != null && isMuleDeploy.equals("true")) {
      phases.put(DEPLOY.id(), mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:deploy"));
    } else {
      phases.put(DEPLOY.id(), mapping.buildGoals("org.apache.maven.plugins:maven-deploy-plugin:2.8.2:deploy"));
    }
    return phases;
  }
}
