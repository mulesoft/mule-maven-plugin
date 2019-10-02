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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mule.tools.api.exception.ValidationException;

import java.io.File;

import static java.lang.String.join;
import static java.nio.file.Paths.get;

public class PolicyYamlValidationTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

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
  public void missingIdYamlFailsParsing() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectMissingProperty("id");
    testYaml("missing-id.yaml");
  }

  @Test
  public void missingNameYamlFailsParsing() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectMissingProperty("name");
    testYaml("missing-name.yaml");
  }

  @Test
  public void missingSupportedPoliciesVersionsYamlSucceeds() throws ValidationException {
    testYaml("missing-supportedPoliciesVersions.yaml");
  }

  @Test
  public void missingDescriptionYamlFailsParsing() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectMissingProperty("description");
    testYaml("missing-description.yaml");
  }

  @Test
  public void missingCategoryYamlFailsParsing() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectMissingProperty("category");
    testYaml("missing-category.yaml");
  }

  @Test
  public void missingTypeYamlFailsParsing() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectMissingProperty("type");
    testYaml("missing-type.yaml");
  }

  @Test
  public void missingResourceLevelSupportedYamlFailsParsing() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectMissingProperty("resourceLevelSupported");
    testYaml("missing-resourceLevelSupported.yaml");
  }

  @Test
  public void missingStandaloneYamlFailsParsing() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectMissingProperty("standalone");
    testYaml("missing-standalone.yaml");
  }

  @Test
  public void missingRequiredCharacteristicsYamlFailsParsing() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectMissingProperty("requiredCharacteristics");
    testYaml("missing-requiredCharacteristics.yaml");
  }

  @Test
  public void missingProvidedCharacteristicsYamlFailsParsing() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectMissingProperty("providedCharacteristics");
    testYaml("missing-providedCharacteristics.yaml");
  }

  @Test
  public void missingConfigurationYamlFailsParsing() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectMissingProperty("configuration");
    testYaml("missing-configuration.yaml");
  }

  @Test
  public void wrongTypeFailsParsing() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("Missing required creator property 'resourceLevelSupported'");
    testYaml("wrong-type.yaml");
  }

  @Test
  public void missingValueYamlFailsParsing() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectedException.expectMessage("Missing required creator property 'requiredCharacteristics'");
    testYaml("missing-value.yaml");
  }

  @Test
  public void missingParameterFromConfigurationItemFailsParsing() throws ValidationException {
    expectedException.expect(ValidationException.class);
    expectMissingProperty("propertyName");
    testYaml("missing-configurationProperty.yaml");
  }

  private void expectMissingProperty(String property) {
    expectedException.expectMessage("Missing required creator property '" + property + "'");
  }

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
