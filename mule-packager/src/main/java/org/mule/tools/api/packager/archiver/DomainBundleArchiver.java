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

import static org.mule.tools.api.packager.structure.FolderNames.APPLICATIONS;
import static org.mule.tools.api.packager.structure.FolderNames.DOMAIN;

import java.io.File;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

/**
 * Defines and creates the basic structure of Mule Domain bundle archive.
 *
 * It saves it in a compressed file.
 */
public class DomainBundleArchiver extends AbstractArchiver {

  public final static String APPLICATIONS_LOCATION = APPLICATIONS.value() + File.separator;

  public final static String DOMAIN_LOCATION = DOMAIN.value() + File.separator;

  public DomainBundleArchiver() {
    this(new ZipArchiver());
  }

  protected DomainBundleArchiver(org.codehaus.plexus.archiver.AbstractArchiver archiver) {
    super(archiver);
  }

  public void addApplications(File resource, String[] includes, String[] excludes) throws ArchiverException {
    addResource(APPLICATIONS_LOCATION, resource, includes, excludes);
  }

  public void addDomain(File resource, String[] includes, String[] excludes) throws ArchiverException {
    addResource(DOMAIN_LOCATION, resource, includes, excludes);
  }
}
