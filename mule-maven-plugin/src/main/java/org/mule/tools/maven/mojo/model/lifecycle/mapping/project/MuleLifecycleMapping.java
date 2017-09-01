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

import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenFactory;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenVersionless;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.*;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.DEPLOY;

public class MuleLifecycleMapping implements LifecycleMapping, ProjectLifecycleMapping {

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
    // This method implementation is to save issues between Maven versions 3.3.3/3.3./3.5.0
    LifecycleMappingMavenVersionless mapping = LifecycleMappingMavenFactory.buildLifecycleMappingMaven(this);
    return mapping.getLifecycles();
  }

  @Override
  public Map getLifecyclePhases(LifecycleMappingMavenVersionless mapping) {
    Map phases = new HashMap<>();
    phases.put(VALIDATE.id(), mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:validate"));
    phases.put(INITIALIZE.id(), mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:initialize"));
    phases.put(GENERATE_SOURCES.id(), mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:generate-sources"));
    phases.put(PROCESS_SOURCES.id(), mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:process-sources"));
    phases.put(PROCESS_RESOURCES.id(),
               mapping.buildGoals("org.apache.maven.plugins:maven-resources-plugin:3.0.2:resources"));

    phases
        .put(COMPILE.id(),
             mapping
                 .buildGoals("org.apache.maven.plugins:maven-compiler-plugin:3.6.1:compile,org.mule.tools.maven:mule-maven-plugin:compile"));
    phases.put(GENERATE_TEST_SOURCES.id(),
               mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:generate-test-sources"));
    phases.put(PROCESS_TEST_RESOURCES.id(),
               mapping.buildGoals("org.apache.maven.plugins:maven-resources-plugin:3.0.2:testResources"));
    phases.put(TEST_COMPILE.id(),
               mapping.buildGoals("org.apache.maven.plugins:maven-compiler-plugin:3.6.1:testCompile"));
    phases.put(TEST.id(), mapping.buildGoals("org.apache.maven.plugins:maven-surefire-plugin:2.19.1:test"));
    phases.put(PACKAGE.id(), mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:package"));
    phases.put(INSTALL.id(), mapping.buildGoals("org.apache.maven.plugins:maven-install-plugin:2.5.2:install"));
    String isMuleDeploy = System.getProperty("muleDeploy");
    if (isMuleDeploy != null && isMuleDeploy.equals("true")) {
      phases.put(DEPLOY.id(), mapping.buildGoals("org.mule.tools.maven:mule-maven-plugin:deploy"));
    } else {
      phases.put(DEPLOY.id(), mapping.buildGoals("org.apache.maven.plugins:maven-deploy-plugin:2.8.2:deploy"));
    }
    return phases;
  }
}
