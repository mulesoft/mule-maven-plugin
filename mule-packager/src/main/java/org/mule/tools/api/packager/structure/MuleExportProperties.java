/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.structure;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class MuleExportProperties {

  public static final String MULE_EXPORT_PROPERTIES_FILE = "mule_export.properties";
  public static final String MULE_EXPORT_VERSION_KEY = "mule_export_version";
  public static final String MULE_EXPORT_VERSION_VALUE = "2.0";
  public static final String MULE_EXPORTED_PROJECTS_KEY = "mule_exported_projects";
  private final Properties muleExportProperties = new Properties();

  public MuleExportProperties(String projectName) {
    muleExportProperties.setProperty(MULE_EXPORT_VERSION_KEY, MULE_EXPORT_VERSION_VALUE);
    muleExportProperties.setProperty(MULE_EXPORTED_PROJECTS_KEY, projectName);
  }

  public void store(File exportableFolder) throws IOException {
    try (FileOutputStream output = new FileOutputStream(new File(exportableFolder, MULE_EXPORT_PROPERTIES_FILE))) {
      muleExportProperties.store(output, null);
    }
  }
}
