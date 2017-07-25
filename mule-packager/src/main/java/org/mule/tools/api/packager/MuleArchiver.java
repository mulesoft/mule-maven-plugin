/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager;

import static org.mule.tools.api.packager.PackagerFolders.CLASSES;
import static org.mule.tools.api.packager.PackagerFolders.MAVEN;
import static org.mule.tools.api.packager.PackagerFolders.META_INF;
import static org.mule.tools.api.packager.PackagerFolders.MULE;
import static org.mule.tools.api.packager.PackagerFolders.MULE_ARTIFACT;
import static org.mule.tools.api.packager.PackagerFolders.MULE_SRC;
import static org.mule.tools.api.packager.PackagerFolders.POLICY;
import static org.mule.tools.api.packager.PackagerFolders.REPOSITORY;

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

  public final static String CLASSES_LOCATION = CLASSES + File.separator;

  public final static String MULE_LOCATION = MULE + File.separator;

  public final static String POLICY_LOCATION = POLICY + File.separator;

  public final static String META_INF_LOCATION = META_INF + File.separator;

  public final static String MAVEN_LOCATION = META_INF_LOCATION + MAVEN + File.separator;

  public final static String MULE_SRC_LOCATION = META_INF_LOCATION + MULE_SRC + File.separator;

  public final static String MULE_ARTIFACT_LOCATION = META_INF_LOCATION + MULE_ARTIFACT + File.separator;

  public static final String REPOSITORY_LOCATION = REPOSITORY + File.separator;


  public void addClasses(File resource, String[] includes, String[] excludes) throws ArchiverException {
    if (resource.isFile()) {
      addFile(resource, CLASSES_LOCATION + resource.getName());
    } else {
      addDirectory(resource, CLASSES_LOCATION, includes, addDefaultExcludes(excludes));
    }
  }

  public void addMuleSrc(File resource, String[] includes, String[] excludes) throws ArchiverException {
    if (resource.isFile()) {
      addFile(resource, MULE_SRC_LOCATION + resource.getName());
    } else {
      addDirectory(resource, MULE_SRC_LOCATION, includes, addDefaultExcludes(excludes));
    }
  }

  public void addMaven(File resource, String[] includes, String[] excludes) throws ArchiverException {
    if (resource.isFile()) {
      addFile(resource, MAVEN_LOCATION + resource.getName());
    } else {
      addDirectory(resource, MAVEN_LOCATION, includes, addDefaultExcludes(excludes));
    }
  }

  public void addMuleArtifact(File resource, String[] includes, String[] excludes) throws ArchiverException {
    if (resource.isFile()) {
      addFile(resource, MULE_ARTIFACT_LOCATION + resource.getName());
    } else {
      addDirectory(resource, MULE_ARTIFACT_LOCATION, includes, addDefaultExcludes(excludes));
    }
  }

  public void addRepository(File resource, String[] includes, String[] excludes) throws ArchiverException {
    if (resource.isFile()) {
      addFile(resource, REPOSITORY_LOCATION + resource.getName());
    } else {
      addDirectory(resource, REPOSITORY_LOCATION, includes, addDefaultExcludes(excludes));
    }
  }

  public void addMule(File resource, String[] includes, String[] excludes) throws ArchiverException {
    if (resource.isFile()) {
      addFile(resource, MULE_LOCATION + resource.getName());
    } else {
      addDirectory(resource, MULE_LOCATION, includes, addDefaultExcludes(excludes));
    }
  }

  public void addPolicy(File resource, String[] includes, String[] excludes) throws ArchiverException {
    if (resource.isFile()) {
      addFile(resource, POLICY_LOCATION + resource.getName());
    } else {
      addDirectory(resource, POLICY_LOCATION, includes, addDefaultExcludes(excludes));
    }
  }

  public void addToRootDirectory(File resource) throws ArchiverException {
    if (resource.isFile()) {
      addFile(resource, ROOT_LOCATION + resource.getName());
    } else {
      addDirectory(resource, ROOT_LOCATION, null, addDefaultExcludes(null));
    }
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
