/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.archiver;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;

import static org.mule.tools.api.packager.structure.FolderNames.MAVEN;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;

/**
 * Defines and creates the basic structure of package file. All package implementations should subclass this archiver.
 */
public class AbstractArchiver extends ZipArchiver {

  public final static String ROOT_LOCATION = StringUtils.EMPTY;

  public final static String META_INF_LOCATION = META_INF.value() + File.separator;

  public final static String MAVEN_LOCATION = META_INF_LOCATION + MAVEN.value() + File.separator;

  /**
   * @param resource Folder or file that is going to be added to the added to the maven folder.
   * @return
   */
  public void addMaven(File resource, String[] includes, String[] excludes) throws ArchiverException {
    addResource(MAVEN_LOCATION, resource, includes, excludes);
  }

  /**
   * @param resource Folder/file that is going to be added to the package root location.
   * @return
   */
  public void addToRoot(File resource, String[] includes, String[] excludes) throws ArchiverException {
    addResource(ROOT_LOCATION, resource, includes, excludes);
  }

  protected String[] addDefaultExcludes(String[] excludes) {
    if ((excludes == null) || (excludes.length == 0)) {
      return DirectoryScanner.DEFAULTEXCLUDES;
    } else {
      String[] newExcludes = new String[excludes.length + DirectoryScanner.DEFAULTEXCLUDES.length];

      System.arraycopy(DirectoryScanner.DEFAULTEXCLUDES, 0, newExcludes, 0, DirectoryScanner.DEFAULTEXCLUDES.length);
      System.arraycopy(excludes, 0, newExcludes, DirectoryScanner.DEFAULTEXCLUDES.length, excludes.length);

      return newExcludes;
    }
  }

  protected void addResource(String resourceLocation, File resource, String[] includes, String[] excludes) {
    if (resource.isFile()) {
      addFile(resource, resourceLocation + resource.getName());
    } else {
      addDirectory(resource, resourceLocation, includes, addDefaultExcludes(excludes));
    }
  }
}
