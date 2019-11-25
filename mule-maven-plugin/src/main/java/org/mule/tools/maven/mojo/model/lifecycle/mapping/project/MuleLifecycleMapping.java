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

import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMaven333;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenFactory;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenVersionless;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.lifecycle.mapping.Lifecycle;
import org.apache.maven.lifecycle.mapping.LifecycleMapping;

public class MuleLifecycleMapping implements LifecycleMapping, ProjectLifecycleMapping {

  private static final String MULE_DEPLOY = "muleDeploy";

  private static final String ORG_APACHE_MAVEN_PLUGINS = "org.apache.maven.plugins";
  private static final String MULE_MAVEN_PLUGIN = "org.mule.tools.maven:mule-maven-plugin";

  private static final String MAVEN_RESOURCES_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-resources-plugin:3.0.2";
  private static final String MAVEN_CLEAN_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-clean-plugin:3.6.1";
  private static final String MAVEN_COMPILER_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-compiler-plugin:3.8.0";
  private static final String MAVEN_SUREFIRE_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-surefire-plugin:2.19.1";
  private static final String MAVEN_INSTALL_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-install-plugin:2.5.2";
  private static final String MAVEN_DEPLOY_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-deploy-plugin:2.8.2";
  private static final String MAVEN_SITE_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-site-plugin:3.8.2";

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
    phases.put(CLEAN.id(), buildGoals(mapping, MAVEN_CLEAN_PLUGIN + ":clean", MULE_MAVEN_PLUGIN + ":clean"));

    phases.put(VALIDATE.id(), buildGoals(mapping, MULE_MAVEN_PLUGIN + ":validate"));
    phases.put(INITIALIZE.id(), buildGoals(mapping, MULE_MAVEN_PLUGIN + ":initialize"));
    phases.put(GENERATE_SOURCES.id(), buildGoals(mapping, MULE_MAVEN_PLUGIN + ":generate-sources"));
    phases.put(PROCESS_SOURCES.id(), buildGoals(mapping, MULE_MAVEN_PLUGIN + ":process-sources"));
    phases.put(PROCESS_RESOURCES.id(),
               buildGoals(mapping, MAVEN_RESOURCES_PLUGIN + ":resources", MULE_MAVEN_PLUGIN + ":process-resources"));
    phases.put(COMPILE.id(), buildGoals(mapping, MAVEN_COMPILER_PLUGIN + ":compile", MULE_MAVEN_PLUGIN + ":compile"));
    phases.put(PROCESS_CLASSES.id(), buildGoals(mapping, MULE_MAVEN_PLUGIN + ":process-classes"));
    phases.put(GENERATE_TEST_SOURCES.id(), buildGoals(mapping, MULE_MAVEN_PLUGIN + ":generate-test-sources"));
    phases.put(PROCESS_TEST_RESOURCES.id(),
               buildGoals(mapping, MAVEN_RESOURCES_PLUGIN + ":testResources", MULE_MAVEN_PLUGIN + ":generate-test-resources"));
    phases.put(TEST_COMPILE.id(),
               buildGoals(mapping, MAVEN_COMPILER_PLUGIN + ":testCompile", MULE_MAVEN_PLUGIN + ":test-compile"));
    phases.put(TEST.id(), buildGoals(mapping, MAVEN_SUREFIRE_PLUGIN + ":test"));
    phases.put(PACKAGE.id(), buildGoals(mapping, MULE_MAVEN_PLUGIN + ":package"));
    phases.put(VERIFY.id(), buildGoals(mapping, MULE_MAVEN_PLUGIN + ":verify"));

    phases.put(INSTALL.id(), buildGoals(mapping, MAVEN_INSTALL_PLUGIN + ":install"));

    phases.put(SITE.id(), buildGoals(mapping, MAVEN_SITE_PLUGIN + ":site", MULE_MAVEN_PLUGIN + ":site"));

    String isMuleDeploy = System.getProperty(MULE_DEPLOY);
    if (isMuleDeploy != null && isMuleDeploy.equals("true")) {
      phases.put(DEPLOY.id(), mapping.buildGoals(MULE_MAVEN_PLUGIN + ":deploy"));
    } else {
      phases.put(DEPLOY.id(), mapping.buildGoals(MAVEN_DEPLOY_PLUGIN + ":deploy"));
    }
    return phases;
  }

  private <V> V buildGoals(LifecycleMappingMavenVersionless mapping, String... goals) {
    return mapping.buildGoals(StringUtils.join(goals, ","));
  }
}
