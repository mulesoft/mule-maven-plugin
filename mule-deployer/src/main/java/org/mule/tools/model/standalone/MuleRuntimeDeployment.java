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

import org.mule.tools.model.Deployment;

import java.io.File;

public interface MuleRuntimeDeployment extends Deployment {

  File getScript();

  void setScript(File script);

  Integer getTimeout();

  void setTimeout(int timeout);

  Long getDeploymentTimeout();

  void setDeploymentTimeout(Long deploymentTimeout);

  String[] getArguments();

  void setArguments(String[] arguments);

  File getMuleHome();

  void setMuleHome(File muleHome);
}
