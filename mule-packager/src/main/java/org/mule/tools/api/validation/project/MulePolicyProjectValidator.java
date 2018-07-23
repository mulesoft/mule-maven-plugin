/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.validation.project;

import static java.lang.String.format;
import static java.lang.String.join;

import org.mule.tools.api.classloader.model.SharedLibraryDependency;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.ProjectInformation;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * Ensures the project is valid
 */
public class MulePolicyProjectValidator extends MuleProjectValidator {

  private static final String[] NECESSARY_FILES = {
      "pom.xml",
      "mule-artifact.json",
      "exchange-template-pom.xml",
      join(File.separator, "src","main", "mule", "template.xml")
  };

  public MulePolicyProjectValidator(ProjectInformation projectInformation, List<SharedLibraryDependency> sharedLibraries,
                                    boolean strictCheck) {
    super(projectInformation, sharedLibraries, strictCheck);
  }

  @Override
  protected void additionalValidation() throws ValidationException {
    isPolicyProjectStructureValid(projectInformation.getProjectBaseFolder());
    super.additionalValidation();
  }

  public static void isPolicyProjectStructureValid(Path projectBaseDir) throws ValidationException {
    allFilesPresent(projectBaseDir);
  }

  private static void allFilesPresent(Path projectBaseDir) throws ValidationException {
    for (String file : NECESSARY_FILES) {
      fileExists(projectBaseDir.toAbsolutePath().toString(), file);
    }
  }

  private static void fileExists(String baseDir, String fileName) throws ValidationException {
    File file = new File(baseDir, fileName);
    if (!file.exists()) {
      throw new ValidationException(format("The file %s should be present.", fileName));
    }
  }

}
