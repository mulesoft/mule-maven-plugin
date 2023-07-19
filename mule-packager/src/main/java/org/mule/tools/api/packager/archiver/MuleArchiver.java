/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.packager.archiver;


import static org.mule.tools.api.packager.structure.FolderNames.MULE_ARTIFACT;
import static org.mule.tools.api.packager.structure.FolderNames.MULE_SRC;
import static org.mule.tools.api.packager.structure.FolderNames.REPOSITORY;

import java.io.File;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

/**
 * Defines and creates the basic structure of Mule archive.
 *
 * It saves it in a compressed file.
 */
public class MuleArchiver extends AbstractArchiver {

  public final static String MULE_SRC_LOCATION = META_INF_LOCATION + MULE_SRC.value() + File.separator;

  public final static String MULE_ARTIFACT_LOCATION = META_INF_LOCATION + MULE_ARTIFACT.value() + File.separator;

  public static final String REPOSITORY_LOCATION = REPOSITORY.value() + File.separator;

  public MuleArchiver() {
    this(new ZipArchiver());
  }

  protected MuleArchiver(org.codehaus.plexus.archiver.AbstractArchiver archiver) {
    super(archiver);
  }

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
