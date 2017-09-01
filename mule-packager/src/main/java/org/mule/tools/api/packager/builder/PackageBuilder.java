/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.builder;


import org.codehaus.plexus.archiver.ArchiverException;

import java.io.File;
import java.io.IOException;

public interface PackageBuilder {

  void createPackage(File destinationFile, String originFolder)
      throws ArchiverException, IOException;

  void createDeployableFile() throws IOException;
}
