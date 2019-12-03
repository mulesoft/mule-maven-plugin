/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.sources;

import static java.io.File.separator;
import static java.lang.String.join;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hamcrest.core.StringContains;

import org.mule.maven.client.api.model.BundleDependency;
import org.mule.tools.api.packager.Pom;
import org.mule.tools.api.packager.structure.ProjectStructure;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;

public class MuleArtifactContentResolverTest {

  private static final String JAR_1 = "jar1.jar";
  private static final String JAR_2 = "jar2.jar";
  private static final String JAR_3_LOCATION = getRelativePath("lala", "lele");
  private static final String JAR_3 = "jar3.jar";
  private static final String CONFIG_1 = "config1.xml";
  private static final String CONFIG_2 = "config2.xml";
  private static final String CONFIG_3_LOCATION = getRelativePath("lolo", "lulu");
  private static final String CONFIG_3 = "config3.xml";

  private static final String COMMON_FILE = "aFile.txt";

  private static final String JAVA_FOLDER_LOCATION = getRelativePath("src", "main", "java");
  private static final String MULE_FOLDER_LOCATION = getRelativePath("src", "main", "mule");
  private static final String MUNIT_FOLDER_LOCATION = getRelativePath("src", "test", "munit");
  private static final String RESOURCES_FOLDER_LOCATION = getRelativePath("src", "main", "resources");
  public static final String HIDDEN_FILE = ".hiddenFile";
  private static final String TEST_RESOURCES_FOLDER_LOCATION = getRelativePath("src", "test", "resources");
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  protected MuleArtifactContentResolver resolver;
  private File javaFolder;
  private File muleFolder;
  private File munitFolder;
  private File resourcesFolder;
  private File testResourcesFolder;
  private String DEFAULT_MULE_CONFIG_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<mule xmlns=\"http://www.mulesoft.org/schema/mule/core\"\n" +
      "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
      "      xmlns:jms=\"http://www.mulesoft.org/schema/mule/jms\"\n" +
      "      xsi:schemaLocation=\"\n" +
      "\thttp://www.mulesoft.org/schema/mule/jms http://www.mulesoft.org/schema/mule/jms/current/mule-jms.xsd\n" +
      "\thttp://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd\">\n" +
      "\n" +
      "\n" +
      "\t\t<xml-module:validate-schema doc:name=\"Validate schema\" doc:id=\"f9656931-d3ca-4969-bf7d-4c8d87fe4918\" config-ref=\"XML_Config\" schemas=\"schemas/shipwire/warehouse/rma/v01/ASN.xsd\"/>\n"
      +
      "</mule>";
  private String MALFORMED_MULE_CONFIG_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<mule xmlns=\"http://www.mulesoft.org/schema/mule/core\"\n" +
      "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
      "      xmlns:jms=\"http://www.mulesoft.org/schema/mule/jms\"\n" +
      "      xsi:schemaLocation=\"\n" +
      "\thttp://www.mulesoft.org/schema/mule/jms http://www.mulesoft.org/schema/mule/jms/current/mule-jms.xsd\n" +
      "\thttp://www.mulesoft.org/schema/mule/core http://www.mulesoft.org/schema/mule/core/current/mule.xsd\">\n" +
      "\n" +
      "\n" +
      "\t\t<xml-module:validate-schema doc:name=\"Validate schema\"config-ref=\"XML_Config\" schemas=\"schemas/shipwire/warehouse/rma/v01/ASN.xsd\"\"/>\n"
      +
      "</mule>";
  private org.w3c.dom.Document documentMock;
  private org.w3c.dom.Element rootElementMock;

  @Before
  public void setUp() throws IOException {
    temporaryFolder.create();
    Pom pomMock = mock(Pom.class);
    resolver = newResolver(new ProjectStructure(temporaryFolder.getRoot().toPath(), false), pomMock,
                           new ArrayList<>());
    javaFolder = new File(temporaryFolder.getRoot(), JAVA_FOLDER_LOCATION);
    muleFolder = new File(temporaryFolder.getRoot(), MULE_FOLDER_LOCATION);
    munitFolder = new File(temporaryFolder.getRoot(), MUNIT_FOLDER_LOCATION);
    resourcesFolder = new File(temporaryFolder.getRoot(), RESOURCES_FOLDER_LOCATION);
    List<Path> resourcesPath = new ArrayList<>();
    resourcesPath.add(resourcesFolder.toPath());
    when(pomMock.getResourcesLocation()).thenReturn(resourcesPath);
    testResourcesFolder = new File(temporaryFolder.getRoot(), TEST_RESOURCES_FOLDER_LOCATION);
    muleFolder.mkdirs();
    munitFolder.mkdirs();
    javaFolder.mkdirs();
    resourcesFolder.mkdirs();
    testResourcesFolder.mkdirs();
    documentMock = mock(org.w3c.dom.Document.class);
    rootElementMock = mock(org.w3c.dom.Element.class);
    when(documentMock.getDocumentElement()).thenReturn(rootElementMock);
  }

  @Test
  public void muleArtifactContentResolverNullPathArgumentInConstructorTest() {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Project structure should not be null");
    newResolver(null, null, null);
  }

  @Test
  public void getExportedPackagesTest() throws IOException {
    File jar1 = new File(javaFolder, JAR_1);
    File jar2 = new File(javaFolder, JAR_2);
    File jar3Folder = new File(javaFolder, JAR_3_LOCATION);
    File jar3 = new File(jar3Folder, JAR_3);
    jar1.createNewFile();
    jar2.createNewFile();
    jar3Folder.mkdirs();
    jar3.createNewFile();

    List<String> actualExportedPackages = resolver.getExportedPackages();

    assertThat("Exported packages does not contain all expected elements", actualExportedPackages,
               containsInAnyOrder(JAR_1, JAR_2, JAR_3_LOCATION + separator + JAR_3));
    assertThat("Exported packages contains more elements than expected", actualExportedPackages.size(), equalTo(3));
  }

  @Test
  public void getExportedResourcesTest() throws IOException {
    File jar1 = new File(resourcesFolder, JAR_1);
    File jar2 = new File(resourcesFolder, JAR_2);
    File jar3Folder = new File(resourcesFolder, JAR_3_LOCATION);
    File jar3 = new File(jar3Folder, JAR_3);
    String configFileName = "config.xml";
    File config = new File(muleFolder, configFileName);

    File hiddenFile = new File(munitFolder, HIDDEN_FILE);
    // Ensure hidden fin in win based systems
    if (System.getProperty("os.name").toLowerCase().equals("win")) {
      Files.setAttribute(hiddenFile.toPath(), "dos:hidden", true);
    }

    jar1.createNewFile();
    jar2.createNewFile();
    jar3Folder.mkdirs();
    jar3.createNewFile();
    config.createNewFile();
    hiddenFile.createNewFile();

    List<String> actualExportedResources = resolver.getExportedResources();

    assertThat("Exported resources does not contain all expected elements", actualExportedResources,
               containsInAnyOrder(JAR_1, JAR_2, JAR_3_LOCATION + separator + JAR_3, configFileName));

    assertThat("Configs contain an unexpected elements", actualExportedResources.contains(hiddenFile), is(false));

    assertThat("Exported resources contains more elements than expected", actualExportedResources.size(), equalTo(4));
  }

  @Test
  public void getTestExportedResourcesTest() throws IOException {
    File jar1 = new File(testResourcesFolder, JAR_1);
    File jar2 = new File(testResourcesFolder, JAR_2);
    File jar3Folder = new File(testResourcesFolder, JAR_3_LOCATION);
    File jar3 = new File(jar3Folder, JAR_3);
    jar1.createNewFile();
    jar2.createNewFile();
    jar3Folder.mkdirs();
    jar3.createNewFile();
    resolver = newResolver(new ProjectStructure(temporaryFolder.getRoot().toPath(), true), mock(Pom.class),
                           new ArrayList<>());
    List<String> actualExportedResources = resolver.getTestExportedResources();

    assertThat("Exported resources does not contain all expected elements", actualExportedResources,
               containsInAnyOrder(JAR_1, JAR_2, JAR_3_LOCATION + separator + JAR_3));
    assertThat("Exported resources contains more elements than expected", actualExportedResources.size(), equalTo(3));
  }

  @Test
  public void getConfigsTest() throws IOException {
    File config1 = new File(muleFolder, CONFIG_1);
    File config2 = new File(muleFolder, CONFIG_2);
    File config3Folder = new File(muleFolder, CONFIG_3_LOCATION);
    File config3 = new File(config3Folder, CONFIG_3);
    File commonFile = new File(muleFolder, COMMON_FILE);

    config1.createNewFile();
    FileUtils.writeStringToFile(config1, DEFAULT_MULE_CONFIG_CONTENT, Charset.defaultCharset());

    config2.createNewFile();
    FileUtils.writeStringToFile(config2, DEFAULT_MULE_CONFIG_CONTENT, Charset.defaultCharset());

    config3Folder.mkdirs();
    config3.createNewFile();
    FileUtils.writeStringToFile(config3, DEFAULT_MULE_CONFIG_CONTENT, Charset.defaultCharset());

    commonFile.createNewFile();


    List<String> actualConfigs = resolver.getConfigs();

    assertThat("Configs does not contain all expected elements", actualConfigs,
               containsInAnyOrder(CONFIG_1, CONFIG_2, CONFIG_3_LOCATION + separator + CONFIG_3));

    assertThat("Configs contain an unexpected elements", actualConfigs.contains(COMMON_FILE), is(false));

    assertThat("Configs contains more elements than expected", actualConfigs.size(), equalTo(3));
  }

  @Test
  public void getConfigsThrowsExceptionIfThereIsAnInvalidConfig() throws Exception {
    File config1 = new File(muleFolder, CONFIG_1);
    File config2 = new File(muleFolder, CONFIG_2);
    String expectedExceptionMessage =
        "Element type \"xml-module:validate-schema\" must be followed by either attribute specifications, \">\" or \"/>\"";

    config1.createNewFile();
    FileUtils.writeStringToFile(config1, DEFAULT_MULE_CONFIG_CONTENT, Charset.defaultCharset());

    config2.createNewFile();
    FileUtils.writeStringToFile(config2, MALFORMED_MULE_CONFIG_CONTENT, Charset.defaultCharset());

    expectedException.expect(RuntimeException.class);
    expectedException.expectMessage(StringContains.containsString(expectedExceptionMessage));
    List<String> actualConfigs = resolver.getConfigs();
  }

  @Test
  public void getTestConfigsTest() throws IOException {
    File config1 = new File(munitFolder, CONFIG_1);
    File config2 = new File(munitFolder, CONFIG_2);
    File config3Folder = new File(munitFolder, CONFIG_3_LOCATION);
    File config3 = new File(config3Folder, CONFIG_3);
    File commonFile = new File(munitFolder, COMMON_FILE);

    config1.createNewFile();
    FileUtils.writeStringToFile(config1, DEFAULT_MULE_CONFIG_CONTENT, Charset.defaultCharset());

    config2.createNewFile();
    FileUtils.writeStringToFile(config2, DEFAULT_MULE_CONFIG_CONTENT, Charset.defaultCharset());

    config3Folder.mkdirs();
    config3.createNewFile();
    FileUtils.writeStringToFile(config3, DEFAULT_MULE_CONFIG_CONTENT, Charset.defaultCharset());

    commonFile.createNewFile();

    resolver = newResolver(new ProjectStructure(temporaryFolder.getRoot().toPath(), true), mock(Pom.class),
                           new ArrayList<>());
    List<String> actualConfigs = resolver.getTestConfigs();

    assertThat("Configs does not contain all expected elements", actualConfigs,
               containsInAnyOrder(CONFIG_1, CONFIG_2, CONFIG_3_LOCATION + separator + CONFIG_3));

    assertThat("Configs contain an unexpected elements", actualConfigs.contains(COMMON_FILE), is(false));

    assertThat("Configs contains more elements than expected", actualConfigs.size(), equalTo(3));
  }

  private static String getRelativePath(String... segments) {
    if (segments != null && segments.length != 0) {
      return join(separator, segments);
    }
    return StringUtils.EMPTY;
  }

  @Test
  public void hasMuleAsRootElementMuleRoot() {
    when(rootElementMock.getTagName()).thenReturn("mule");
    assertThat("Method should have returned true", resolver.hasMuleAsRootElement(documentMock));
  }

  @Test
  public void hasMuleAsRootElementMuleDomainRoot() {
    when(rootElementMock.getTagName()).thenReturn("mule-domain");
    assertThat("Method should have returned true", resolver.hasMuleAsRootElement(documentMock));
  }

  @Test
  public void hasMuleAsRootElementOtherThanMuleOrMuleDomainRoot() {
    when(rootElementMock.getTagName()).thenReturn("mulesoft");
    assertThat("Method should have returned false", !resolver.hasMuleAsRootElement(documentMock));
  }

  @Test
  public void hasMuleAsRootElementWithNullName() {
    when(rootElementMock.getTagName()).thenReturn(null);
    assertThat("Method should have returned false", !resolver.hasMuleAsRootElement((Document) null));
  }

  @Test
  public void hasMuleAsRootElementWithNoRoot() {
    when(documentMock.getDocumentElement()).thenReturn(null);
    assertThat("Method should have returned false", !resolver.hasMuleAsRootElement(documentMock));
  }

  @Test
  public void hasMuleAsRootElementWithNullDocument() {
    assertThat("Method should have returned false", !resolver.hasMuleAsRootElement((Document) null));
  }

  protected MuleArtifactContentResolver newResolver(ProjectStructure projectStructure, Pom pomMock,
                                                    List<BundleDependency> objects) {
    return new MuleArtifactContentResolver(projectStructure, pomMock, objects);
  }

}
