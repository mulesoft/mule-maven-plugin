/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.artifact.archiver.internal;

import static org.mule.tools.artifact.archiver.api.PackagerConstants.*;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Creates the structure and archive for a Mule Application
 */
public class MuleArchiver extends ZipArchiver {

  public final static String ROOT_LOCATION = StringUtils.EMPTY;

  public final static String CLASSES_LOCATION = CLASSES_FOLDER + File.separator;

  public final static String MULE_LOCATION = MULE_FOLDER + File.separator;

  public final static String META_INF_LOCATION = META_INF_FOLDER + File.separator;

  public final static String MAVEN_LOCATION = META_INF_LOCATION + MAVEN_FOLDER + File.separator;

  public final static String MULE_SRC_LOCATION = META_INF_LOCATION + MULE_SRC_FOLDER + File.separator;

  public final static String MULE_ARTIFACT_LOCATION = META_INF_LOCATION + MULE_ARTIFACT_FOLDER + File.separator;

  public static final String REPOSITORY_LOCATION = REPOSITORY_FOLDER + File.separator;


  public void addClasses(File file) throws ArchiverException {
    addFile(file, CLASSES_LOCATION + file.getName());
  }

  public void addClasses(File directoryName, String[] includes, String[] excludes) throws ArchiverException {
    addDirectory(directoryName, CLASSES_LOCATION, includes, addDefaultExcludes(excludes));
  }

  public void addMuleSrc(File file) throws ArchiverException {
    addFile(file, MULE_SRC_LOCATION + file.getName());
  }

  public void addMuleSrc(File directoryName, String[] includes, String[] excludes) throws ArchiverException {
    addDirectory(directoryName, MULE_SRC_LOCATION, includes, addDefaultExcludes(excludes));
  }

  public void addMaven(File file) throws ArchiverException {
    addFile(file, MAVEN_LOCATION + file.getName());
  }

  public void addMaven(File directoryName, String[] includes, String[] excludes) throws ArchiverException {
    addDirectory(directoryName, MAVEN_LOCATION, includes, addDefaultExcludes(excludes));
  }

  public void addMuleArtifact(File file) throws ArchiverException {
    addFile(file, MULE_ARTIFACT_LOCATION + file.getName());
  }

  public void addMuleArtifact(File directoryName, String[] includes, String[] excludes) throws ArchiverException {
    addDirectory(directoryName, MULE_ARTIFACT_LOCATION, includes, addDefaultExcludes(excludes));
  }

  public void addRepository(File file) throws ArchiverException {
    addFile(file, REPOSITORY_LOCATION + file.getName());
  }

  public void addRepository(File directoryName, String[] includes, String[] excludes) throws ArchiverException {
    addDirectory(directoryName, REPOSITORY_LOCATION, includes, addDefaultExcludes(excludes));
  }

  public void addMule(File file) throws ArchiverException {
    addFile(file, MULE_LOCATION + file.getName());
  }

  public void addMule(File directoryName, String[] includes, String[] excludes) throws ArchiverException {
    addDirectory(directoryName, MULE_LOCATION, includes, addDefaultExcludes(excludes));
  }

  public void addRootDirectory(File directory) throws ArchiverException {
    addDirectory(directory, ROOT_LOCATION, null, addDefaultExcludes(null));
  }

  @Deprecated
  public void addRootFile(File file) throws ArchiverException {
    addFile(file, ROOT_LOCATION + file.getName());
  }

  private String[] addDefaultExcludes(String[] excludes) {
    if ((excludes == null) || (excludes.length == 0)) {
      return DirectoryScanner.DEFAULTEXCLUDES;
    } else {
      String[] newExcludes = new String[excludes.length + DirectoryScanner.DEFAULTEXCLUDES.length];

      System.arraycopy(DirectoryScanner.DEFAULTEXCLUDES, 0, newExcludes, 0, DirectoryScanner.DEFAULTEXCLUDES.length);
      System.arraycopy(excludes, 0, newExcludes, DirectoryScanner.DEFAULTEXCLUDES.length, excludes.length);

      return newExcludes;
    }
  }
}
