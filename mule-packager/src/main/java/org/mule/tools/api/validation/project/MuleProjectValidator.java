/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.project;

import java.io.File;
import java.nio.file.Path;

import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;

import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_DEPLOY_PROPERTIES;

/**
 * Ensures the project is valid
 */
public class MuleProjectValidator extends AbstractProjectValidator {


  public MuleProjectValidator(ProjectInformation projectInformation) {
    super(projectInformation);
  }

  @Override
  protected void additionalValidation() throws ValidationException {
    isProjectStructureValid(projectInformation.getPackaging(), projectInformation.getProjectBaseFolder());
  }

  /**
   * It validates the project folder structure is valid
   * 
   * @return true if the project's structure is valid
   * @throws ValidationException if the project structure is invalid
   */
  public static void isProjectStructureValid(String packagingType, Path projectBaseDir) throws ValidationException {
    File mainSrcApplication = mainSrcApplication(packagingType, projectBaseDir);
    if (!mainSrcApplication.exists()) {
      throw new ValidationException("The folder " + mainSrcApplication.getAbsolutePath() + " is mandatory");
    }
    File muleDeployProperties = new File(mainSrcApplication, MULE_DEPLOY_PROPERTIES);
    if (!muleDeployProperties.exists()) {
      throw new ValidationException("The file " + muleDeployProperties.getAbsolutePath() + " is mandatory");
    }
  }

  private static File mainSrcApplication(String packagingType, Path projectBaseDir) throws ValidationException {
    return PackagingType.fromString(packagingType).getSourceFolderLocation(projectBaseDir).toFile();
  }
}
