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

import org.apache.maven.lifecycle.mapping.Lifecycle;
import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.apache.maven.lifecycle.mapping.LifecyclePhase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.maven.plugins.annotations.LifecyclePhase.*;

public class MuleLifecycleMapping implements LifecycleMapping {

  private static final String DEFAULT_LIFECYCLE_ID = "default";

  @Override
  public List<String> getOptionalMojos(String lifecycle) {
    return null;
  }

  @Override
  public Map<String, String> getPhases(String lifecycle) {
    return null;
  }

  @Override
  public Map<String, Lifecycle> getLifecycles() {
    Map<String, Lifecycle> lifecycleMap = new HashMap<>();
    Lifecycle lifecycle = getDefaultLifecycle();
    lifecycleMap.put(lifecycle.getId(), lifecycle);
    return lifecycleMap;
  }

  private Lifecycle getDefaultLifecycle() {
    Lifecycle lifecycle = new Lifecycle();
    lifecycle.setId(DEFAULT_LIFECYCLE_ID);
    lifecycle.setLifecyclePhases(getLifecyclePhases());
    return lifecycle;
  }

  private Map<String, LifecyclePhase> getLifecyclePhases() {
    Map<String, LifecyclePhase> phases = new HashMap<>();
    phases.put(VALIDATE.id(), createLifecyclePhase("org.mule.tools.maven:mule-maven-plugin:validate"));
    phases.put(INITIALIZE.id(), createLifecyclePhase("org.mule.tools.maven:mule-maven-plugin:initialize"));
    phases.put(GENERATE_SOURCES.id(), createLifecyclePhase("org.mule.tools.maven:mule-maven-plugin:generate-sources"));
    phases.put(PROCESS_SOURCES.id(), createLifecyclePhase("org.mule.tools.maven:mule-maven-plugin:process-sources"));
    phases.put(PROCESS_RESOURCES.id(),
               createLifecyclePhase("org.apache.maven.plugins:maven-resources-plugin:3.0.2:resources"));
    phases.put(COMPILE.id(), createLifecyclePhase("org.apache.maven.plugins:maven-compiler-plugin:3.6.1:compile"));
    phases.put(GENERATE_TEST_SOURCES.id(),
               createLifecyclePhase("org.mule.tools.maven:mule-maven-plugin:generate-test-sources"));
    phases.put(PROCESS_TEST_RESOURCES.id(),
               createLifecyclePhase("org.apache.maven.plugins:maven-resources-plugin:3.0.2:testResources"));
    phases.put(TEST_COMPILE.id(),
               createLifecyclePhase("org.apache.maven.plugins:maven-compiler-plugin:3.6.1:testCompile"));
    phases.put(TEST.id(), createLifecyclePhase("org.apache.maven.plugins:maven-surefire-plugin:2.19.1:test"));
    phases.put(PACKAGE.id(), createLifecyclePhase("org.mule.tools.maven:mule-maven-plugin:package"));
    phases.put(INSTALL.id(), createLifecyclePhase("org.apache.maven.plugins:maven-install-plugin:2.5.2:install"));
    phases.put(DEPLOY.id(), createLifecyclePhase("org.apache.maven.plugins:maven-deploy-plugin:2.8.2:deploy"));
    return phases;
  }

  private LifecyclePhase createLifecyclePhase(String... goals) {
    LifecyclePhase phase = new LifecyclePhase();
    phase.set(String.join(",", Arrays.asList(goals)));
    return phase;
  }
}
