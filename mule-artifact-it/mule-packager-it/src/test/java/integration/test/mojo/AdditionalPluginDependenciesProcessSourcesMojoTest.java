/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class AdditionalPluginDependenciesProcessSourcesMojoTest extends AbstractProcessSourcesMojoTest {

  private static final String GENERATED__DEPENDENCY_X_PATH_SUFFIX =
      "/target/repository/group/id/x/artifact-id-x/1.0.0/artifact-id-x-1.0.0.jar";
  private static final String GENERATED__DEPENDENCY_X_V2_PATH_SUFFIX =
      "/target/repository/group/id/x/artifact-id-x/2.0.0/artifact-id-x-2.0.0.jar";
  private static final String GENERATED__DEPENDENCY_X_V3_PATH_SUFFIX =
      "/target/repository/group/id/x/artifact-id-x/3.0.0/artifact-id-x-3.0.0.jar";
  private static final String GENERATED__DEPENDENCY_Y_PATH_SUFFIX =
      "/target/repository/group/id/y/artifact-id-y/1.0.0/artifact-id-y-1.0.0.jar";
  private static final String GENERATED__DEPENDENCY_Z_PATH_SUFFIX =
      "/target/repository/group/id/z/artifact-id-z/1.0.0/artifact-id-z-1.0.0.jar";
  private static final String GENERATED__DEPENDENCY_Z_V2_PATH_SUFFIX =
      "/target/repository/group/id/z/artifact-id-z/2.0.0/artifact-id-z-2.0.0.jar";


  private static final String GENERATED_CLASSLOADER_MODEL_PATH_COMMON_SUFFIX =
      "/target/META-INF/mule-artifact/classloader-model.json";


  @Test
  void muleAppWithPluginWithAdditionalDependency() throws Exception {
    final String appName = "mule-app-plugin-with-additional-dep";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertThat(generatedAppClassLoaderModelFileContent).isEqualTo(expectedAppClassLoaderModelFileContent);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Y_PATH_SUFFIX);
  }

  @Test
  void muleAppWithPluginWithDependencyAndAdditionalDependencyWithSameVersion() throws Exception {
    final String appName = "mule-app-plugin-with-dep-and-additional-dep-same-version";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertThat(generatedAppClassLoaderModelFileContent).isEqualTo(expectedAppClassLoaderModelFileContent);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
  }

  @Test
  void muleAppWithPluginWithDependencyAndAdditionalDependencyWithDifferentVersion() throws Exception {
    final String appName = "mule-app-plugin-with-dep-and-additional-dep-different-version";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertThat(generatedAppClassLoaderModelFileContent).isEqualTo(expectedAppClassLoaderModelFileContent);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_V2_PATH_SUFFIX);
  }

  @Test
  void muleAppWithMultiplePluginsWithAdditionalDependencies() throws Exception {
    final String appName = "mule-app-multiple-plugins-with-additional-deps";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertThat(generatedAppClassLoaderModelFileContent).isEqualTo(expectedAppClassLoaderModelFileContent);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Y_PATH_SUFFIX);
  }


  @Test
  void muleAppWithMultiplePluginsWithSameAdditionalDependencies() throws Exception {
    final String appName = "mule-app-multiple-plugins-with-same-additional-dep";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertThat(generatedAppClassLoaderModelFileContent).isEqualTo(expectedAppClassLoaderModelFileContent);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Y_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Z_PATH_SUFFIX);
  }

  @Test
  void muleAppWithMultiplePluginsWithSameAdditionalDependenciesWithDifferentVersion() throws Exception {
    final String appName = "mule-app-multiple-plugins-with-same-additional-dep-different-version";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertThat(generatedAppClassLoaderModelFileContent).isEqualTo(expectedAppClassLoaderModelFileContent);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Y_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Z_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Z_V2_PATH_SUFFIX);
  }

  /**
   * Validates that if a mule-plugin defined additional libraries those are taken into account.
   */
  @Test
  void muleAppWithReusableAppPlugin() throws Exception {
    final String appName = "mule-app-plugin-with-reusable-app";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertThat(generatedAppClassLoaderModelFileContent).isEqualTo(expectedAppClassLoaderModelFileContent);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Z_PATH_SUFFIX);
  }

  /**
   * Validates that additional libraries defined at the mule application level overrides all other additional libraries from mule
   * plugins.
   */
  @Test
  void muleAppWithReusableAppPluginAndOverridedAdditionalDependency() throws Exception {
    final String appName = "mule-app-plugin-with-reusable-app-and-additional-dep-x-v2";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertThat(generatedAppClassLoaderModelFileContent).isEqualTo(expectedAppClassLoaderModelFileContent);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_V2_PATH_SUFFIX);
  }

  /**
   * Validates that if two mule-plugin have the same additional library then the latest one is going to be used.
   */
  @Test
  void muleAppWithReusableAppAndUsingAnotherReusableAppWithANewestXVersion() throws Exception {
    final String appName = "mule-app-plugin-with-reusable-app-and-reusable-app2";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertThat(generatedAppClassLoaderModelFileContent).isEqualTo(expectedAppClassLoaderModelFileContent);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_V3_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Y_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Z_PATH_SUFFIX);
  }

  @Test
  void muleAppWithPluginWithSameDependencyAsAdditionalDifferentClassifier() throws Exception {
    final String appName = "mule-app-plugin-with-same-additional-dep-different-classifier";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertThat(generatedAppClassLoaderModelFileContent).isEqualTo(expectedAppClassLoaderModelFileContent);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Y_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, "/target/repository/group/id/y/artifact-id-y/1.0.0/artifact-id-y-1.0.0-test-jar.jar");
  }

  @Test
  void muleAppWithPluginWithSameDependencyAsAdditionalDifferentClassifierAsTransitive() throws Exception {
    final String appName = "mule-app-plugin-with-same-additional-dep-different-classifier-as-transitive";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertThat(generatedAppClassLoaderModelFileContent).isEqualTo(expectedAppClassLoaderModelFileContent);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Y_PATH_SUFFIX);
    assertArtifactInstalled(appLocation,
                            "/target/repository/group/id/y/artifact-with-y-as-test-jar/1.0.0/artifact-with-y-as-test-jar-1.0.0.jar");
    assertArtifactInstalled(appLocation, "/target/repository/group/id/y/artifact-id-y/1.0.0/artifact-id-y-1.0.0-test-jar.jar");
  }


  private String getAppLocation(String appName) {
    return "/additional-plugin-dependencies/" + appName;
  }

  private String getCorrectGeneratedClassloaderModelPath(String appName) {
    return appName + GENERATED_CLASSLOADER_MODEL_PATH_COMMON_SUFFIX;
  }

  private void assertArtifactInstalled(String appLocation, String location) throws Exception {
    String finalLocation = appLocation + location;
    try {
      assertThat(getFile(finalLocation)).exists();
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  private String getCorrectExpectedClassloaderModelPath(String appLocation, String fileName) {
    return appLocation + "/expected-files/" + fileName + ".json";
  }

}
