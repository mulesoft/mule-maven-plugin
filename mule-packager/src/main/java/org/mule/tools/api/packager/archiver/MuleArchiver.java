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


import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_SRC;
import static org.mule.tools.api.packager.structure.FolderNames.REPOSITORY;

import java.io.File;

import org.codehaus.plexus.archiver.ArchiverException;

/**
 * Defines and creates the basic structure of Mule .jar file
 */
public class MuleArchiver extends Archiver {

  public final static String MULE_SRC_LOCATION = META_INF_LOCATION + MULE_SRC.value() + File.separator;

  public final static String MULE_ARTIFACT_LOCATION = META_INF_LOCATION + MULE_ARTIFACT.value() + File.separator;

  public static final String REPOSITORY_LOCATION = REPOSITORY.value() + File.separator;


  public void addMuleSrc(File resource, String[] includes, String[] excludes) throws ArchiverException {
    addResource(MULE_SRC_LOCATION, resource, includes, excludes);
  }

  public void addMuleArtifact(File resource, String[] includes, String[] excludes) throws ArchiverException {
    addResource(MULE_ARTIFACT_LOCATION, resource, includes, excludes);
  }

  public void addRepository(File resource, String[] includes, String[] excludes) throws ArchiverException {
    addResource(REPOSITORY_LOCATION, resource, includes, excludes);
  }
}
