/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

public class AdditionalPluginDependenciesProcessSourcesMojoTest extends ProcessSourcesMojoTest {

  private static final String GENERATED_PLUGIN_WITH_DEPENDENCY_X_CLASSLOADER_MODEL_PATH_SUFFIX =
      "/target/repository/org/mule/group/mule-plugin-with-dependency-x/1.0.0/classloader-model.json";
  private static final String GENERATED_PLUGIN_WITH_DEPENDENCY_Y_CLASSLOADER_MODEL_PATH_SUFFIX =
      "/target/repository/org/mule/group/mule-plugin-with-dependency-y/1.0.0/classloader-model.json";

  private static final String EXPECTED_PLUGIN_WITH_DEPENDENCY_X_CLASSLOADER_MODEL_PATH =
      "/additional-plugin-dependencies/mule-plugin-with-dependency-x/expected-files/mule-plugin-with-dependency-x.json";
  private static final String EXPECTED_PLUGIN_WITH_DEPENDENCY_Y_CLASSLOADER_MODEL_PATH =
      "/additional-plugin-dependencies/mule-plugin-with-dependency-y/expected-files/mule-plugin-with-dependency-y.json";

  private static final String GENERATED_CLASSLOADER_MODEL_PATH_COMMON_SUFFIX =
      "/target/META-INF/mule-artifact/classloader-model.json";


  @Test
  public void muleAppWithPluginWithAdditionalDependency() throws Exception {
    final String appName = "mule-app-plugin-with-additional-dep";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    List<String> generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    List<String> expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertThat(generatedAppClassLoaderModelFileContent, equalTo(expectedAppClassLoaderModelFileContent));

    List<String> generatedPluginClassLoaderModelFileContent =
        getFileContent(appLocation + GENERATED_PLUGIN_WITH_DEPENDENCY_X_CLASSLOADER_MODEL_PATH_SUFFIX);
    List<String> expectedPluginClassLoaderModelFileContent =
        getFileContent(EXPECTED_PLUGIN_WITH_DEPENDENCY_X_CLASSLOADER_MODEL_PATH);
    assertThat(generatedPluginClassLoaderModelFileContent, equalTo(expectedPluginClassLoaderModelFileContent));
  }

  @Test
  public void muleAppWithMultiplePluginsWithAdditionalDependencies() throws Exception {
    final String appName = "mule-app-multiple-plugins-with-additional-deps";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    List<String> generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    List<String> expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertThat(generatedAppClassLoaderModelFileContent, equalTo(expectedAppClassLoaderModelFileContent));

    List<String> generatedPluginClassLoaderModelFileContent =
        getFileContent(appLocation + GENERATED_PLUGIN_WITH_DEPENDENCY_X_CLASSLOADER_MODEL_PATH_SUFFIX);
    List<String> expectedPluginClassLoaderModelFileContent =
        getFileContent(EXPECTED_PLUGIN_WITH_DEPENDENCY_X_CLASSLOADER_MODEL_PATH);
    assertThat(generatedPluginClassLoaderModelFileContent, equalTo(expectedPluginClassLoaderModelFileContent));

    generatedPluginClassLoaderModelFileContent =
        getFileContent(appLocation + GENERATED_PLUGIN_WITH_DEPENDENCY_Y_CLASSLOADER_MODEL_PATH_SUFFIX);
    expectedPluginClassLoaderModelFileContent =
        getFileContent(EXPECTED_PLUGIN_WITH_DEPENDENCY_Y_CLASSLOADER_MODEL_PATH);
    assertThat(generatedPluginClassLoaderModelFileContent, equalTo(expectedPluginClassLoaderModelFileContent));
  }


  @Test
  public void muleAppWithMultiplePluginsWithSameAdditionalDependencies() throws Exception {
    final String appName = "mule-app-multiple-plugins-with-additional-deps";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    List<String> generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    List<String> expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertThat(generatedAppClassLoaderModelFileContent, equalTo(expectedAppClassLoaderModelFileContent));

    List<String> generatedPluginClassLoaderModelFileContent =
        getFileContent(appLocation + GENERATED_PLUGIN_WITH_DEPENDENCY_X_CLASSLOADER_MODEL_PATH_SUFFIX);
    List<String> expectedPluginClassLoaderModelFileContent =
        getFileContent(EXPECTED_PLUGIN_WITH_DEPENDENCY_X_CLASSLOADER_MODEL_PATH);
    assertThat(generatedPluginClassLoaderModelFileContent, equalTo(expectedPluginClassLoaderModelFileContent));

    generatedPluginClassLoaderModelFileContent =
        getFileContent(appLocation + GENERATED_PLUGIN_WITH_DEPENDENCY_Y_CLASSLOADER_MODEL_PATH_SUFFIX);
    expectedPluginClassLoaderModelFileContent =
        getFileContent(EXPECTED_PLUGIN_WITH_DEPENDENCY_Y_CLASSLOADER_MODEL_PATH);
    assertThat(generatedPluginClassLoaderModelFileContent, equalTo(expectedPluginClassLoaderModelFileContent));
  }

  private String getAppLocation(String appName) {
    return "/additional-plugin-dependencies/" + appName;
  }

  private String getCorrectGeneratedClassloaderModelPath(String appName) {
    return appName + GENERATED_CLASSLOADER_MODEL_PATH_COMMON_SUFFIX;
  }

  private String getCorrectExpectedClassloaderModelPath(String appLocation, String fileName) {
    return appLocation + "/expected-files/" + fileName + ".json";
  }

  protected void installThirdPartyArtifact(String groupId, String artifactId, String version, String type,
                                           String dependencyProjectName)
      throws IOException, VerificationException {
    File dependencyProjectRootFolder = builder.createProjectBaseDir(dependencyProjectName, this.getClass());
    Verifier auxVerifier = buildVerifier(dependencyProjectRootFolder);
    auxVerifier.deleteArtifact(groupId, artifactId, version, type);
    auxVerifier.assertArtifactNotPresent(groupId, artifactId, version, type);
    auxVerifier.executeGoal(INSTALL);
    auxVerifier.verifyErrorFreeLog();
  }

}
