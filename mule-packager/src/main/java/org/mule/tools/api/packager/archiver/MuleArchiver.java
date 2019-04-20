/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.archiver;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

import java.io.File;

import static org.mule.tools.api.packager.structure.FolderNames.API;
import static org.mule.tools.api.packager.structure.FolderNames.CLASSES;
import static org.mule.tools.api.packager.structure.FolderNames.LIB;
import static org.mule.tools.api.packager.structure.FolderNames.META_INF;

/**
 * Defines and creates the basic structure of Mule archive.
 *
 * It saves it in a compressed file.
 */
public class MuleArchiver extends AbstractArchiver {

  private static final String CLASSES_LOCATION = CLASSES.value() + File.separator;
  private static final String API_LOCATION = API.value() + File.separator;
  private static final String LIB_LOCATION = LIB.value() + File.separator;
  private static final String META_INF_LOCATION = META_INF.value() + File.separator;

  public MuleArchiver() {
    this(new ZipArchiver());
  }

  protected MuleArchiver(org.codehaus.plexus.archiver.AbstractArchiver archiver) {
    super(archiver);
  }

  /**
   * @param resource Folder or file that is going to be added to the added to the classes folder.
   * @return
   */
  public void addClasses(File resource, String[] includes, String[] excludes) throws ArchiverException {
    addResource(CLASSES_LOCATION, resource, includes, excludes);
  }

  /**
   * @param resource Folder or file that is going to be added to META_INF folder.
   * @return
   */
  public void addMetaInf(File resource, String[] includes, String[] excludes) throws ArchiverException {
    if (resource != null && resource.exists() && resource.list() != null && resource.list().length != 0) {
      addResource(META_INF_LOCATION, resource, includes, excludes);
    }
  }

  /**
   * @param resource Folder that is going to be added to the root and its contents are going to be added to the classes folder.
   * @return
   */
  public void addApi(File resource, String[] includes, String[] excludes) throws ArchiverException {
    if (resource != null && resource.exists() && resource.list() != null && resource.list().length != 0) {
      addResource(API_LOCATION, resource, includes, excludes);
      addClasses(resource, includes, excludes);
    }
  }

  /**
   * @param resource Folder which contents are going to be added to the classes folder.
   * @return
   */
  public void addWsdl(File resource, String[] includes, String[] excludes) throws ArchiverException {
    if (resource != null && resource.exists() && resource.list() != null && resource.list().length != 0) {
      addClasses(resource, includes, excludes);
    }
  }

  /**
   * @param resource Folder which contents are going to be added to the lib folder.
   * @return
   */
  public void addLib(File resource, String[] includes, String[] excludes) {
    if (resource != null && resource.exists() && resource.list() != null && resource.list().length != 0) {
      addResource(LIB_LOCATION, resource, includes, excludes);
    }
  }

  /**
   * @param resource Folder which contents are going to be added to the classes folder.
   * @return
   */
  public void addMappings(File resource, String[] includes, String[] excludes) {
    if (resource != null && resource.exists() && resource.list() != null && resource.list().length != 0) {
      addClasses(resource, includes, excludes);
    }
  }
}
