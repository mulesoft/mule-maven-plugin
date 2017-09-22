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

import java.io.IOException;
import java.nio.file.Path;

import org.codehaus.plexus.archiver.ArchiverException;

/**
 * Builder for packages.
 */
public interface PackageBuilder {

  /**
   * Creates a package and leaves that in the destinationPath. It does so based on a folder naming convention, it will look for
   * the resources properly named in the originFolderPath.
   * 
   * @param originFolderPath folder location where to look for resources. It's expected that the folder structure follows a
   *        predefined structure.
   * @param destinationPath path where to leave the created package.
   * @throws ArchiverException
   * @throws IOException
   */
  void createPackage(Path originFolderPath, Path destinationPath) throws ArchiverException, IOException;

  /**
   * Creates a package and leaves that in the destinationPath.
   * 
   * @param destinationPath path where to leave the created package.
   * @throws ArchiverException
   * @throws IOException
   */
  void createPackage(Path destinationPath) throws ArchiverException, IOException;

}
