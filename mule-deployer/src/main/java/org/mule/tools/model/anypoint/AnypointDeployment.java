/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.model.anypoint;

import org.mule.tools.model.Deployment;

public interface AnypointDeployment extends Deployment {

  public String getUsername();

  public void setUsername(String username);

  public String getPassword();

  public void setPassword(String password);

  public String getServer();

  public void setServer(String server);

  String getEnvironment();

  String getBusinessGroup();

  String getUri();
}
