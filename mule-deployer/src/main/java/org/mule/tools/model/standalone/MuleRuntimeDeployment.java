/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.model.standalone;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.model.Deployment;

import java.io.File;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class MuleRuntimeDeployment extends Deployment {

  public abstract File getScript();

  public abstract void setScript(File script);

  @Deprecated
  public abstract Integer getTimeout();

  @Deprecated
  public abstract void setTimeout(int timeout);

  public abstract String[] getArguments();

  public abstract void setArguments(String[] arguments);

  public abstract File getMuleHome();

  public abstract void setMuleHome(File muleHome);

  @Override
  public void setEnvironmentSpecificValues() throws DeploymentException {
    String scriptLocation = getProperty("mule.script");
    if (isNotBlank(scriptLocation)) {
      setScript(new File(scriptLocation));
    }

    String timeout = getProperty("mule.timeout");
    if (isNotBlank(timeout)) {
      setTimeout(Integer.valueOf(timeout));
    }

    String arguments = getProperty("mule.arguments");
    if (isNotBlank(arguments)) {
      setArguments(arguments.split(","));
    }

    String muleHome = getProperty("mule.home");
    if (isNotBlank(muleHome)) {
      setMuleHome(new File(muleHome));
    }
    if (getMuleHome() == null) {
      throw new DeploymentException("Invalid deployment configuration, missing mule home value. Please set it either through the plugin configuration or -Dmule.home when building the current project");
    }
  }

}
