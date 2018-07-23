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

import static java.nio.file.Paths.get;
import static org.mule.tools.api.validation.project.MulePolicyProjectValidator.isPolicyProjectStructureValid;

import org.mule.tools.api.exception.ValidationException;

import java.io.File;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MulePolicyProjectValidatorTest {

  private static final String RESOURCES_FOLDER = "policy-validation";
  private static final String MISSING_MULE_ARTIFACT = concatPath(RESOURCES_FOLDER, "missing-mule-artifact");
  private static final String HAPPY_PATH = concatPath(RESOURCES_FOLDER, "happy-path");

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void isPolicyProjectStructureIsValid() throws ValidationException {
    isPolicyProjectStructureValid(getTestResourceFolder(HAPPY_PATH));
  }

  @Test(expected = ValidationException.class)
  public void isPolicyProjectStructureIsInvalidWithoutMuleArtifact() throws ValidationException {
    isPolicyProjectStructureValid(getTestResourceFolder(MISSING_MULE_ARTIFACT));
  }

  private Path getTestResourceFolder(String folderName) {
    return get(concatPath("target", "test-classes", folderName));
  }

  private static String concatPath(String... parts) {
    String result = "";

    for (String part : parts) {
      result += part + File.separator;
    }

    return result;
  }

}
