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

  String getUsername();

  void setUsername(String username);

  String getPassword();

  void setPassword(String password);

  String getServer();

  void setServer(String server);

  String getEnvironment();

  void setEnvironment(String environment);

  String getBusinessGroup();

  void setBusinessGroup(String property);

  String getUri();

  void setUri(String uri);
}
