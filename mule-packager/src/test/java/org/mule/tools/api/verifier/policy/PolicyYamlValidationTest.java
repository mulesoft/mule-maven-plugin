/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.verifier.policy;

import org.assertj.core.util.introspection.CaseFormatUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mule.tools.api.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.io.File;

import static java.lang.String.join;
import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
  public void missingSupportedPoliciesVersionsYamlSucceeds() throws ValidationException {
    testYaml("missing-supportedPoliciesVersions.yaml");
  }

  @ParameterizedTest(name = "missing{0}YamlFailsParsing")
  @ValueSource(strings = {"Description", "Category", "Type", "Name", "Id", "Standalone", "ResourceLevelSupported",
      "RequiredCharacteristics", "ProvidedCharacteristics", "Configuration"})
  public void missingPropertyYamlFailsParsing(String property) {
    final String propertyName = CaseFormatUtils.toCamelCase(property);
    assertThatThrownBy(() -> testYaml("missing-" + propertyName + ".yaml"))
        .isExactlyInstanceOf(ValidationException.class)
        .hasMessageContaining(expectMissingProperty(propertyName));
  }

  @Test
  public void missingValueYamlFailsParsing() {
    assertThatThrownBy(() -> testYaml("missing-value.yaml"))
        .isExactlyInstanceOf(ValidationException.class)
        .hasMessageContaining(expectMissingProperty("requiredCharacteristics"));
  }

  @Test
  public void wrongTypeFailsParsing() {
    assertThatThrownBy(() -> testYaml("wrong-type.yaml"))
        .isExactlyInstanceOf(ValidationException.class)
        .hasMessageContaining(expectMissingProperty("resourceLevelSupported"));
  }

  @Test
  public void missingParameterFromConfigurationItemFailsParsing() throws ValidationException {
    assertThatThrownBy(() -> testYaml("missing-configurationProperty.yaml"))
        .isExactlyInstanceOf(ValidationException.class)
        .hasMessageContaining(expectMissingProperty("propertyName"));
  }

  private String expectMissingProperty(String property) {
    return "Missing required creator property '" + property + "'";
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
