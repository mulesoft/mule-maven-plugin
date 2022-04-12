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

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMaven;

import java.util.Map;

import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.CLEAN;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.COMPILE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.DEPLOY;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.GENERATE_SOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.GENERATE_TEST_SOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.INITIALIZE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.INSTALL;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PACKAGE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_CLASSES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_RESOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_SOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_TEST_RESOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.SITE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.TEST;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.TEST_COMPILE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.VALIDATE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.VERIFY;

public class MuleLifecycleMapping extends AbstractLifecycleMapping {

  @Override
  public <V> Map<String, V> getLifecyclePhases(LifecycleMappingMaven mapping) {
    ImmutableMap.Builder<String, V> phases = ImmutableMap.builder();
    phases.put(CLEAN.id(), buildGoals(mapping, cleanGoal(), muleGoal("clean")));
    phases.put(VALIDATE.id(), buildGoals(mapping, muleGoal("validate")));
    phases.put(INITIALIZE.id(), buildGoals(mapping, muleGoal("initialize")));
    phases.put(GENERATE_SOURCES.id(), buildGoals(mapping, muleGoal("generate-sources")));
    phases.put(PROCESS_SOURCES.id(), buildGoals(mapping, muleGoal("process-sources")));
    phases.put(PROCESS_RESOURCES.id(), buildGoals(mapping, resourceGoal("resources"), muleGoal("process-resources")));
    phases.put(COMPILE.id(), buildGoals(mapping, compilerGoal("compile"), muleGoal("compile")));
    phases.put(PROCESS_CLASSES.id(), buildGoals(mapping, muleGoal("process-classes")));
    phases.put(GENERATE_TEST_SOURCES.id(), buildGoals(mapping, muleGoal("generate-test-sources")));
    phases.put(PROCESS_TEST_RESOURCES.id(),
               buildGoals(mapping, resourceGoal("testResources"), muleGoal("generate-test-resources")));
    phases.put(TEST_COMPILE.id(), buildGoals(mapping, compilerGoal("testCompile"), muleGoal("test-compile")));
    phases.put(TEST.id(), buildGoals(mapping, surefireGoal()));
    phases.put(PACKAGE.id(), buildGoals(mapping, muleGoal("package")));
    phases.put(VERIFY.id(), buildGoals(mapping, muleGoal("verify")));
    phases.put(INSTALL.id(), buildGoals(mapping, installGoal()));
    phases.put(SITE.id(), buildGoals(mapping, siteGoal(), muleGoal("site")));

    if (Boolean.parseBoolean(System.getProperty(MULE_DEPLOY))) {
      phases.put(DEPLOY.id(), buildGoals(mapping, muleGoal("deploy")));
    } else {
      phases.put(DEPLOY.id(),
                 buildGoals(mapping, exchangeGoal("exchange-pre-deploy"), deployGoal(), exchangeGoal("exchange-deploy")));
    }

    return phases.build();
  }

  private <V> V buildGoals(LifecycleMappingMaven mapping, String... goals) {
    return mapping.buildGoals(StringUtils.join(goals, ","));
  }
}
