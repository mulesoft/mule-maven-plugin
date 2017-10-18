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

public interface Deployment {

  public File getApplication();

  public void setApplication(File application);

  public String getApplicationName();

  public void setApplicationName(String applicationName);

  public String getSkip();

  public void setSkip(String skip);

  public Optional<String> getMuleVersion();

  public void setMuleVersion(String muleVersion);
}
