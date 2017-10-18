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
/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.model.DeploymentConfiguration;
import org.mule.tools.utils.DeployerLog;

public class AgentDeployment extends DeploymentConfiguration {

  @Parameter(readonly = true, property = "anypoint.uri", defaultValue = "https://anypoint.mulesoft.com")
  protected String uri;

  public AgentDeployment(AgentDeployment deploymentConfiguration, DeployerLog log) {
    super();
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
