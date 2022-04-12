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
import org.apache.maven.lifecycle.mapping.Lifecycle;
import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.apache.maven.lifecycle.mapping.LifecyclePhase;
import org.mule.tools.maven.mojo.model.lifecycle.mapping.version.LifecycleMappingMaven;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractLifecycleMapping implements LifecycleMapping, ProjectLifecycleMapping {

  private static final Map<Class<?>, Function<Object, String>> TO_STRING = ImmutableMap
      .of(
          String.class, value -> (String) value,
          LifecyclePhase.class, Object::toString);

  protected static final String MULE_DEPLOY = "muleDeploy";

  protected static final String ORG_APACHE_MAVEN_PLUGINS = "org.apache.maven.plugins";
  protected static final String MULE_MAVEN_PLUGIN = "org.mule.tools.maven:mule-maven-plugin";
  protected static final String EXCHANGE_PLUGIN = "org.mule.tools.maven";

  protected static final String MAVEN_RESOURCES_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-resources-plugin:3.0.2";
  protected static final String MAVEN_CLEAN_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-clean-plugin:3.1.0";
  protected static final String MAVEN_COMPILER_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-compiler-plugin:3.8.1";
  protected static final String MAVEN_SUREFIRE_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-surefire-plugin:2.19.1";
  protected static final String MAVEN_INSTALL_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-install-plugin:2.5.2";
  protected static final String MAVEN_DEPLOY_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-deploy-plugin:2.8.2";
  protected static final String MAVEN_SITE_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-site-plugin:3.8.2";
  protected static final String EXCHANGE_PUBLICATION_PLUGIN = EXCHANGE_PLUGIN + ":exchange-mule-maven-plugin:0.0.17";

  @Override
  public List<String> getOptionalMojos(String lifecycle) {
    return Collections.emptyList();
  }

  @Override
  public Map<String, String> getPhases(String lifecycle) {
    return getLifecyclePhases(new LifecycleMappingMaven(this)).entrySet().stream()
        .collect(Collectors.toMap(
                                  Map.Entry::getKey,
                                  entry -> Optional.ofNullable(entry.getValue())
                                      .map(value -> TO_STRING.getOrDefault(value.getClass(), String::valueOf).apply(value))
                                      .orElse("")));
  }

  @Override
  public Map<String, Lifecycle> getLifecycles() {
    return new LifecycleMappingMaven(this).getLifecycles();
  }

  protected String muleGoal(String goal) {
    return MULE_MAVEN_PLUGIN + ":" + goal;
  }

  protected String cleanGoal() {
    return MAVEN_CLEAN_PLUGIN + ":clean";
  }

  protected String resourceGoal(String goal) {
    return MAVEN_RESOURCES_PLUGIN + ":" + goal;
  }

  protected String installGoal() {
    return MAVEN_INSTALL_PLUGIN + ":install";
  }

  protected String siteGoal() {
    return MAVEN_SITE_PLUGIN + ":site";
  }

  protected String deployGoal() {
    return MAVEN_DEPLOY_PLUGIN + ":deploy";
  }

  protected String compilerGoal(String goal) {
    return MAVEN_COMPILER_PLUGIN + ":" + goal;
  }

  protected String surefireGoal() {
    return MAVEN_SUREFIRE_PLUGIN + ":test";
  }

  protected String exchangeGoal(String goal) {
    return EXCHANGE_PUBLICATION_PLUGIN + ":" + goal;
  }
}
