/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model;

import java.io.File;
import java.util.Optional;

import org.apache.maven.plugins.annotations.Parameter;

public class DeploymentConfiguration implements Deployment {

  // TODO change name to artifact
  @Parameter(property = "mule.application")
  protected File application; // VALIDATIONS REQURIED

  @Parameter(readonly = true, property = "applicationName")
  protected String applicationName;

  // TODO validate what for?
  @Parameter(property = "mule.skip")
  protected String skip;

  @Parameter(property = "mule.version")
  protected String muleVersion;

  /**
   * Application file to be deployed.
   *
   * @since 1.0
   */
  public File getApplication() {
    return application;
  }

  public void setApplication(File application) {
    this.application = application;
  }

  /**
   * Name of the application to deploy/undeploy. If not specified, the artifact id will be used as the name. This parameter allows
   * to override this behavior to specify a custom name.
   *
   * @since 2.0
   */
  public String getApplicationName() {
    return applicationName;
  }

  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  public String getSkip() {
    return skip;
  }

  public void setSkip(String skip) {
    this.skip = skip;
  }

  @Override
  public Optional<String> getMuleVersion() {
    return Optional.ofNullable(this.muleVersion);
  }

  @Override
  public void setMuleVersion(String muleVersion) {
    this.muleVersion = muleVersion;
  }

}
