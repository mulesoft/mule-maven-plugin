package org.mule.tools.api.validation.yaml;

import static java.lang.String.join;
import static java.nio.file.Paths.get;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PolicyYamlValidationTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void normalYamlParsesCorrectly() throws IOException {
    testYaml("caching-yaml-example.yaml");
  }

  @Test
  public void minimalYamlParsesCorrectly() throws IOException {
    testYaml("caching-yaml-example.yaml");
  }

  @Test
  public void extraFieldParsesCorrectly() throws IOException {
    testYaml("caching-yaml-example.yaml");
  }


  @Test
  public void missingIdYamlFailsParsing() throws IOException {
    expectedException.expect(IOException.class);
    expectMissingProperty("id");
    testYaml("missing-id.yaml");
  }

  @Test
  public void missingNameYamlFailsParsing() throws IOException {
    expectedException.expect(IOException.class);
    expectMissingProperty("name");
    testYaml("missing-name.yaml");
  }

  @Test
  public void missingSupportedPoliciesVersionsYamlFailsParsing() throws IOException {
    expectedException.expect(IOException.class);
    expectMissingProperty("supportedPoliciesVersions");
    testYaml("missing-supportedPoliciesVersions.yaml");
  }

  @Test
  public void missingDescriptionYamlFailsParsing() throws IOException {
    expectedException.expect(IOException.class);
    expectMissingProperty("description");
    testYaml("missing-description.yaml");
  }

  @Test
  public void missingCategoryYamlFailsParsing() throws IOException {
    expectedException.expect(IOException.class);
    expectMissingProperty("category");
    testYaml("missing-category.yaml");
  }

  @Test
  public void missingTypeYamlFailsParsing() throws IOException {
    expectedException.expect(IOException.class);
    expectMissingProperty("type");
    testYaml("missing-type.yaml");
  }

  @Test
  public void missingResourceLevelSupportedYamlFailsParsing() throws IOException {
    expectedException.expect(IOException.class);
    expectMissingProperty("resourceLevelSupported");
    testYaml("missing-resourceLevelSupported.yaml");
  }

  @Test
  public void missingStandaloneYamlFailsParsing() throws IOException {
    expectedException.expect(IOException.class);
    expectMissingProperty("standalone");
    testYaml("missing-standalone.yaml");
  }

  @Test
  public void missingRequiredCharacteristicsYamlFailsParsing() throws IOException {
    expectedException.expect(IOException.class);
    expectMissingProperty("requiredCharacteristics");
    testYaml("missing-requiredCharacteristics.yaml");
  }

  @Test
  public void missingProvidedCharacteristicsYamlFailsParsing() throws IOException {
    expectedException.expect(IOException.class);
    expectMissingProperty("providedCharacteristics");
    testYaml("missing-providedCharacteristics.yaml");
  }

  @Test
  public void missingConfigurationYamlFailsParsing() throws IOException {
    expectedException.expect(IOException.class);
    expectMissingProperty("configuration");
    testYaml("missing-configuration.yaml");
  }

  @Test
  public void wrongTypeFailsParsing() throws IOException {
    expectedException.expect(IOException.class);
    expectedException.expectMessage("Cannot deserialize value of type `java.lang.Boolean`");
    testYaml("wrong-type.yaml");
  }

  @Test
  public void missingValueYamlFailsParsing() throws IOException {
    expectedException.expect(IOException.class);
    expectedException.expectMessage("Null value for creator property 'requiredCharacteristics'");
    testYaml("missing-value.yaml");
  }

  private void expectMissingProperty(String property) {
    expectedException.expectMessage("Missing required creator property '" + property + "'");
  }

  private void testYaml(String file) throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true);
    mapper.readValue(new File(getTestResourceFolder(), file), PolicyYaml.class);
  }

  private String getTestResourceFolder() {
    return get(concatPath("target", "test-classes", "policy-validation", "yaml-validation-examples")).toAbsolutePath().toString();
  }

  private static String concatPath(String... parts) {
    return join(File.separator, parts);
  }

}
