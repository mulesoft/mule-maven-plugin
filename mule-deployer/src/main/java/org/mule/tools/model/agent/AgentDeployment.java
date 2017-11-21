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
import org.mule.tools.client.standalone.exception.DeploymentException;
import org.mule.tools.model.Deployment;

import java.io.File;
import java.util.Optional;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class AgentDeployment extends Deployment {

  @Parameter
  protected String uri;

  @Override
  public void setEnvironmentSpecificValues() throws DeploymentException {
    String anypointUri = getProperty("anypoint.uri");
    if (isNotBlank(anypointUri)) {
      setUri(anypointUri);
    }
    if (isBlank(getUri())) {
      setUri("https://anypoint.mulesoft.com");
    }
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
