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

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mule.tools.client.AbstractMuleClient.DEFAULT_BASE_URL;
import static org.mule.tools.model.anypoint.AnypointDeployment.ANYPOINT_BASE_URI;

public class AgentDeployment extends Deployment {

  /**
   * Anypoint Platform URI, can be configured to use with On Premise platform..
   * 
   * @since 2.0.0
   */
  @Parameter
  protected String uri;


  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  @Override
  public void setEnvironmentSpecificValues() throws DeploymentException {
    // TODO why we use a prop if this is a parameter ?
    String anypointUri = getProperty(ANYPOINT_BASE_URI);
    if (isNotBlank(anypointUri)) {
      setUri(anypointUri);
    }
    if (isBlank(getUri())) {
      setUri(DEFAULT_BASE_URL);
    }
  }
}
