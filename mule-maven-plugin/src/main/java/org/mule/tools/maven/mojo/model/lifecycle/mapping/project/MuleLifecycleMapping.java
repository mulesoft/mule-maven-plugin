/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.model.lifecycle.mapping.project;

import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenFactory;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMavenVersionless;

public class MuleLifecycleMapping implements LifecycleMapping, ProjectLifecycleMapping {

  private static final String MULE_DEPLOY = "muleDeploy";

  private static final String ORG_APACHE_MAVEN_PLUGINS = "org.apache.maven.plugins";
  private static final String MULE_MAVEN_PLUGIN = "org.mule.tools.maven:mule-maven-plugin";

  private static final String MAVEN_RESOURCES_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-resources-plugin:3.0.2";
  private static final String MAVEN_CLEAN_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-clean-plugin:3.6.1";
  private static final String MAVEN_COMPILER_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-compiler-plugin:3.6.1";
  private static final String MAVEN_SUREFIRE_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-surefire-plugin:2.19.1";
  private static final String MAVEN_INSTALL_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-install-plugin:2.5.2";
  private static final String MAVEN_DEPLOY_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-deploy-plugin:2.8.2";
  private static final String MAVEN_SITE_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-site-plugin:3.6.1";

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
    phases.put(CLEAN.id(), buildGoals(mapping, MAVEN_CLEAN_PLUGIN + ":clean", MULE_MAVEN_PLUGIN + ":clean"));
    phases.put(VALIDATE.id(), buildGoals(mapping, MULE_MAVEN_PLUGIN + ":validate"));
    phases.put(INITIALIZE.id(), buildGoals(mapping, MULE_MAVEN_PLUGIN + ":initialize"));

    phases.put(GENERATE_SOURCES.id(), buildGoals(mapping, MULE_MAVEN_PLUGIN + ":generate-sources"));
    phases.put(PROCESS_SOURCES.id(), buildGoals(mapping, MULE_MAVEN_PLUGIN + ":process-sources"));

    phases.put(PROCESS_RESOURCES.id(), buildGoals(mapping, MAVEN_RESOURCES_PLUGIN + ":resources"));

    phases.put(COMPILE.id(), buildGoals(mapping, MAVEN_COMPILER_PLUGIN + ":compile", MULE_MAVEN_PLUGIN + ":compile"));

    phases.put(PROCESS_TEST_RESOURCES.id(), buildGoals(mapping, MAVEN_RESOURCES_PLUGIN + ":testResources"));
    phases.put(TEST_COMPILE.id(),
               buildGoals(mapping, MAVEN_COMPILER_PLUGIN + ":testCompile", MULE_MAVEN_PLUGIN + ":test-compile"));
    phases.put(TEST.id(), buildGoals(mapping, MAVEN_SUREFIRE_PLUGIN + ":test"));

    phases.put(PACKAGE.id(), buildGoals(mapping, MULE_MAVEN_PLUGIN + ":package"));
    phases.put(INSTALL.id(), buildGoals(mapping, MAVEN_INSTALL_PLUGIN + ":install"));

    phases.put(SITE.id(), buildGoals(mapping, MAVEN_SITE_PLUGIN + ":site", MULE_MAVEN_PLUGIN + ":site"));

    String isMuleDeploy = System.getProperty("muleDeploy");
    if (isMuleDeploy != null && isMuleDeploy.equals("true")) {
      phases.put(DEPLOY.id(), mapping.buildGoals(MULE_MAVEN_PLUGIN + ":deploy"));
    } else {
      phases.put(DEPLOY.id(), mapping.buildGoals(MAVEN_DEPLOY_PLUGIN + ":deploy"));
    }
    return phases;
  }

  private Object buildGoals(LifecycleMappingMavenVersionless mapping, String... goals) {
    return mapping.buildGoals(StringUtils.join(goals, ","));
  }
}
