/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.verifier.policy;

import org.mule.tools.api.exception.ValidationException;
import org.junit.jupiter.api.Test;
import java.io.File;

import static java.lang.String.join;
import static java.nio.file.Paths.get;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PolicyYamlValidationTest {

  @Test
  public void normalYamlParsesCorrectly() throws ValidationException {
    // Example of yaml file extracted from the http caching policy.
    testYaml("caching-yaml-example.yaml");
  }

  @Test
  public void minimalYamlParsesCorrectly() throws ValidationException {
    // A yaml with only the minimum amount necessary of fields.
    testYaml("minimal.yaml");
  }

  @Test
  public void extraFieldParsesCorrectly() throws ValidationException {
    // A yaml with only the minimum amount necessary of fields and an extra one.
    testYaml("extra-field.yaml");
  }

  @Test
  public void missingIdYamlFailsParsing() {
    Exception exception = assertThrows(ValidationException.class, () -> testYaml("missing-id.yaml"));
    String expectedMessage = "Missing required creator property id";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void missingNameYamlFailsParsing() {
    Exception exception = assertThrows(ValidationException.class, () -> testYaml("missing-name.yaml"));
    String expectedMessage = "Missing required creator property name";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void missingSupportedPoliciesVersionsYamlSucceeds() throws ValidationException {
    testYaml("missing-supportedPoliciesVersions.yaml");
  }

  @Test
  public void missingDescriptionYamlFailsParsing() {
    Exception exception = assertThrows(ValidationException.class, () -> testYaml("missing-description.yaml"));
    String expectedMessage = "Missing required creator property description";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void missingCategoryYamlFailsParsing() {
    Exception exception = assertThrows(ValidationException.class, () -> testYaml("missing-category.yaml"));
    String expectedMessage = "Missing required creator property category";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void missingTypeYamlFailsParsing() {
    Exception exception = assertThrows(ValidationException.class, () -> testYaml("missing-type.yaml"));
    String expectedMessage = "Missing required creator property type";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void missingResourceLevelSupportedYamlFailsParsing() {
    Exception exception = assertThrows(ValidationException.class, () -> testYaml("missing-resourceLevelSupported.yaml"));
    String expectedMessage = "Missing required creator property resourceLevelSupported";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void missingStandaloneYamlFailsParsing() {
    Exception exception = assertThrows(ValidationException.class, () -> testYaml("missing-standalone.yaml"));
    String expectedMessage = "Missing required creator property standalone";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void missingRequiredCharacteristicsYamlFailsParsing() {
    Exception exception = assertThrows(ValidationException.class, () -> testYaml("missing-requiredCharacteristics.yaml"));
    String expectedMessage = "Missing required creator property requiredCharacteristics";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void missingProvidedCharacteristicsYamlFailsParsing() {
    Exception exception = assertThrows(ValidationException.class, () -> testYaml("missing-providedCharacteristics.yaml"));
    String expectedMessage = "Missing required creator property providedCharacteristics";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void missingConfigurationYamlFailsParsing() {
    Exception exception = assertThrows(ValidationException.class, () -> testYaml("missing-configuration.yaml"));
    String expectedMessage = "Missing required creator property configuration";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void wrongTypeFailsParsing() {
    Exception exception = assertThrows(ValidationException.class, () -> testYaml("wrong-type.yaml"));
    String expectedMessage = "Missing required creator property 'resourceLevelSupported'";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void missingValueYamlFailsParsing() {
    Exception exception = assertThrows(ValidationException.class, () -> testYaml("missing-value.yaml"));
    String expectedMessage = "Missing required creator property 'requiredCharacteristics'";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  public void missingParameterFromConfigurationItemFailsParsing() {
    Exception exception = assertThrows(ValidationException.class, () -> {
      testYaml("missing-configurationProperty.yaml");
    });
    String expectedMessage = "Missing required creator property propertyName";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  //  private void expectMissingProperty(String property) {
  //    expectedException.expectMessage("Missing required creator property '" + property + "'");
  //  }

  private void testYaml(String file) throws ValidationException {
    new PolicyYamlVerifier(getTestResourceFolder(), file).validate();
  }

  private String getTestResourceFolder() {
    return get(concatPath("target", "test-classes", "policy-validation", "yaml-validation-examples")).toAbsolutePath().toString();
  }

  private static String concatPath(String... parts) {
    return join(File.separator, parts);
  }

}
