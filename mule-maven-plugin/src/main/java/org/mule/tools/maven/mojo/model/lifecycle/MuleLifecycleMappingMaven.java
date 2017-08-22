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

import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.COMPILE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.DEPLOY;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.GENERATE_SOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.GENERATE_TEST_SOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.INITIALIZE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.INSTALL;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PACKAGE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_RESOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_SOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_TEST_RESOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.TEST;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.TEST_COMPILE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.VALIDATE;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.lifecycle.mapping.Lifecycle;

public abstract class MuleLifecycleMappingMaven {

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

  protected Map getLifecyclePhases() {
    Map phases = new HashMap<>();
    phases.put(VALIDATE.id(), buildGoals("org.mule.tools.maven:mule-maven-plugin:validate"));
    phases.put(INITIALIZE.id(), buildGoals("org.mule.tools.maven:mule-maven-plugin:initialize"));
    phases.put(GENERATE_SOURCES.id(), buildGoals("org.mule.tools.maven:mule-maven-plugin:generate-sources"));
    phases.put(PROCESS_SOURCES.id(), buildGoals("org.mule.tools.maven:mule-maven-plugin:process-sources"));
    phases.put(PROCESS_RESOURCES.id(),
               buildGoals("org.apache.maven.plugins:maven-resources-plugin:3.0.2:resources"));

    phases
        .put(COMPILE.id(),
             buildGoals("org.apache.maven.plugins:maven-compiler-plugin:3.6.1:compile,org.mule.tools.maven:mule-maven-plugin:compile"));
    phases.put(GENERATE_TEST_SOURCES.id(),
               buildGoals("org.mule.tools.maven:mule-maven-plugin:generate-test-sources"));
    phases.put(PROCESS_TEST_RESOURCES.id(),
               buildGoals("org.apache.maven.plugins:maven-resources-plugin:3.0.2:testResources"));
    phases.put(TEST_COMPILE.id(),
               buildGoals("org.apache.maven.plugins:maven-compiler-plugin:3.6.1:testCompile"));
    phases.put(TEST.id(), buildGoals("org.apache.maven.plugins:maven-surefire-plugin:2.19.1:test"));
    phases.put(PACKAGE.id(), buildGoals("org.mule.tools.maven:mule-maven-plugin:package"));
    phases.put(INSTALL.id(), buildGoals("org.apache.maven.plugins:maven-install-plugin:2.5.2:install"));
    String isMuleDeploy = System.getProperty("muleDeploy");
    if (isMuleDeploy != null && isMuleDeploy.equals("true")) {
      phases.put(DEPLOY.id(), buildGoals("org.mule.tools.maven:mule-maven-plugin:deploy"));
    } else {
      phases.put(DEPLOY.id(), buildGoals("org.apache.maven.plugins:maven-deploy-plugin:2.8.2:deploy"));
    }
    return phases;
  }

  protected abstract Object buildGoals(String goals);

  protected abstract void setLifecyclePhases(Lifecycle lifecycle);
}
