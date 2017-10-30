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

  File getArtifact();

  void setArtifact(File application);

  String getApplicationName();

  void setApplicationName(String applicationName);

  String getSkip();

  void setSkip(String skip);

  Optional<String> getMuleVersion();

  void setMuleVersion(String muleVersion);
}
