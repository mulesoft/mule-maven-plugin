/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.project;

import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;
import static org.mule.tools.api.packager.structure.FolderNames.*;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_DEPLOY_PROPERTIES;
import static org.mule.tools.api.validation.project.MuleProjectValidator.isProjectStructureValid;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import org.mule.tools.api.exception.ValidationException;

public class MuleProjectValidatorTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();
  @Rule
  public TemporaryFolder projectBuildFolder = new TemporaryFolder();

  @Test(expected = ValidationException.class)
  public void projectStructureValidMuleApplicationInvalid() throws ValidationException {
    Path mainSrcFolder = projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value());
    mainSrcFolder.toFile().mkdirs();

    isProjectStructureValid(MULE.toString(), projectBaseFolder.getRoot().toPath());
  }



  @Test
  public void isProjectStructureValidMuleApplication() throws ValidationException, IOException {
    Path muleMainSrcFolder =
        projectBaseFolder.getRoot().toPath().resolve(SRC.value()).resolve(MAIN.value()).resolve(APP.value());
    muleMainSrcFolder.toFile().mkdirs();
    assumeTrue(new File(muleMainSrcFolder.toFile(), MULE_DEPLOY_PROPERTIES).createNewFile());
    isProjectStructureValid(MULE.toString(), projectBaseFolder.getRoot().toPath());
  }

}
