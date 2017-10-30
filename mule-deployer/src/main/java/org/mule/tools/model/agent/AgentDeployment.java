/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.model.agent;

import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.model.Deployment;

import java.io.File;
import java.util.Optional;

public class AgentDeployment implements Deployment {

  @Parameter
  protected String uri;

  // TODO change name to artifact
  @Parameter
  protected File application; // VALIDATIONS REQURIED

  @Parameter
  protected String applicationName;

  // TODO validate what for?
  @Parameter
  protected String skip;

  @Parameter
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

  public Optional<String> getMuleVersion() {
    return Optional.ofNullable(this.muleVersion);
  }

  public void setMuleVersion(String muleVersion) {
    this.muleVersion = muleVersion;
  }

  /**
   * Anypoint Platform URI, can be configured to use with On Premise platform..
   *
   * @since 2.0
   */
  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
}
