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

import static java.lang.String.join;
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
  private static final String HAPPY_PATH = concatPath(RESOURCES_FOLDER, "happy-path");
  private static final String MISSING_MULE_ARTIFACT = concatPath(RESOURCES_FOLDER, "missing-mule-artifact");
  private static final String MISSING_EXCHANGE_TEMPLATE_POM = concatPath(RESOURCES_FOLDER, "missing-exchange-template-pom");
  private static final String MISSING_TEMPLATE_XML = concatPath(RESOURCES_FOLDER, "missing-template-xml");
  private static final String MISSING_YAML = concatPath(RESOURCES_FOLDER, "missing-yaml");
  private static final String MISSING_YAML2 = concatPath(RESOURCES_FOLDER, "missing-yaml2");


  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void isPolicyProjectStructureIsValid() throws ValidationException {
    isPolicyProjectStructureValid(getTestResourceFolder(HAPPY_PATH));
  }

  @Test
  public void isPolicyProjectStructureIsInvalidWithoutMuleArtifact() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("The file mule-artifact.json should be present.");
    isPolicyProjectStructureValid(getTestResourceFolder(MISSING_MULE_ARTIFACT));
  }

  @Test
  public void isPolicyProjectStructureIsInvalidWithoutExchangeTemplatePom() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("The file exchange-template-pom.xml should be present.");
    isPolicyProjectStructureValid(getTestResourceFolder(MISSING_EXCHANGE_TEMPLATE_POM));
  }

  @Test
  public void isPolicyProjectStructureIsInvalidWithoutTemplateXML() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("The file " + concatPath("src", "main", "mule", "template.xml") + " should be present.");
    isPolicyProjectStructureValid(getTestResourceFolder(MISSING_TEMPLATE_XML));
  }

  @Test
  public void isPolicyProjectStructureIsInvalidWithMissingYaml() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("The file custom.policy.test.yaml should be present.");
    isPolicyProjectStructureValid(getTestResourceFolder(MISSING_YAML));
  }

  @Test
  public void isPolicyProjectStructureIsInvalidWithMissingYaml2() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("The file custom.policy.test2.yaml should be present.");
    isPolicyProjectStructureValid(getTestResourceFolder(MISSING_YAML2));
  }

  private Path getTestResourceFolder(String folderName) {
    return get(concatPath("target", "test-classes", folderName));
  }

  private static String concatPath(String... parts) {
    return join(File.separator, parts);
  }

}
