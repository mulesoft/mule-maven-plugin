/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo.model.lifecycle.mapping.project;
/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

public class LifecyclePluginsGAVs {

  private static final String ORG_APACHE_MAVEN_PLUGINS = "org.apache.maven.plugins";
  private static final String EXCHANGE_PLUGIN = "org.mule.tools.maven";

  static final String MAVEN_RESOURCES_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-resources-plugin:3.3.1";
  static final String MAVEN_CLEAN_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-clean-plugin:3.3.1";
  static final String MAVEN_COMPILER_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-compiler-plugin:3.11.0";
  static final String MAVEN_SUREFIRE_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-surefire-plugin:3.1.2";
  static final String MAVEN_INSTALL_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-install-plugin:3.1.1";
  static final String MAVEN_DEPLOY_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-deploy-plugin:3.1.1";
  static final String MAVEN_SITE_PLUGIN = ORG_APACHE_MAVEN_PLUGINS + ":maven-site-plugin:4.0.0-M10";

  static final String EXCHANGE_PUBLICATION_PLUGIN = EXCHANGE_PLUGIN + ":exchange-mule-maven-plugin:0.1.4";

  private LifecyclePluginsGAVs() {}
}
