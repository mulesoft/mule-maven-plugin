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
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMaven;

import java.util.Map;

import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.DEPLOY;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.GENERATE_RESOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.GENERATE_SOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.INITIALIZE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.INSTALL;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PACKAGE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.PROCESS_RESOURCES;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.VALIDATE;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.VERIFY;

public class DomainBundleLifecycleMapping extends AbstractLifecycleMapping {

  @Override
  public <V> Map<String, V> getLifecyclePhases(LifecycleMappingMaven mapping) {
    ImmutableMap.Builder<String, V> phases = ImmutableMap.builder();
    phases.put(VALIDATE.id(), mapping.buildGoals(muleGoal("validate")));
    phases.put(INITIALIZE.id(), mapping.buildGoals(muleGoal("initialize")));
    phases.put(GENERATE_RESOURCES.id(), mapping.buildGoals(muleGoal("generate-resources")));
    phases.put(PROCESS_RESOURCES.id(), mapping.buildGoals(muleGoal("process-resources")));
    phases.put(GENERATE_SOURCES.id(), mapping.buildGoals(muleGoal("generate-sources")));
    phases.put(PACKAGE.id(), mapping.buildGoals(muleGoal("package")));
    phases.put(VERIFY.id(), mapping.buildGoals(muleGoal("verify")));
    phases.put(INSTALL.id(), mapping.buildGoals(installGoal()));

    if (Boolean.parseBoolean(System.getProperty(MULE_DEPLOY))) {
      phases.put(DEPLOY.id(), mapping.buildGoals(muleGoal("deploy")));
    } else {
      phases.put(DEPLOY.id(), mapping.buildGoals(deployGoal()));
    }

    return phases.build();
  }
}
