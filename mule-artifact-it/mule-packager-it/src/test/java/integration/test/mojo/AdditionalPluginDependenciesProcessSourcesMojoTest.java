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
import static org.junit.Assert.fail;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import org.junit.Test;

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
  public void muleAppWithPluginWithAdditionalDependency() throws Exception {
    final String appName = "mule-app-plugin-with-additional-dep";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertEquals(generatedAppClassLoaderModelFileContent, expectedAppClassLoaderModelFileContent, true);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Y_PATH_SUFFIX);
  }

  @Test
  public void muleAppWithPluginWithDependencyAndAdditionalDependencyWithSameVersion() throws Exception {
    final String appName = "mule-app-plugin-with-dep-and-additional-dep-same-version";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertEquals(generatedAppClassLoaderModelFileContent, expectedAppClassLoaderModelFileContent, true);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
  }

  @Test
  public void muleAppWithPluginWithDependencyAndAdditionalDependencyWithDifferentVersion() throws Exception {
    final String appName = "mule-app-plugin-with-dep-and-additional-dep-different-version";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertEquals(generatedAppClassLoaderModelFileContent, expectedAppClassLoaderModelFileContent, true);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_V2_PATH_SUFFIX);
  }

  @Test
  public void muleAppWithMultiplePluginsWithAdditionalDependencies() throws Exception {
    final String appName = "mule-app-multiple-plugins-with-additional-deps";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertEquals(generatedAppClassLoaderModelFileContent, expectedAppClassLoaderModelFileContent, true);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Y_PATH_SUFFIX);
  }


  @Test
  public void muleAppWithMultiplePluginsWithSameAdditionalDependencies() throws Exception {
    final String appName = "mule-app-multiple-plugins-with-same-additional-dep";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertEquals(generatedAppClassLoaderModelFileContent, expectedAppClassLoaderModelFileContent, true);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Y_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Z_PATH_SUFFIX);
  }

  @Test
  public void muleAppWithMultiplePluginsWithSameAdditionalDependenciesWithDifferentVersion() throws Exception {
    final String appName = "mule-app-multiple-plugins-with-same-additional-dep-different-version";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertEquals(generatedAppClassLoaderModelFileContent, expectedAppClassLoaderModelFileContent, true);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Y_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Z_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Z_V2_PATH_SUFFIX);
  }

  /**
   * Validates that if a mule-plugin defined additional libraries those are taken into account.
   */
  @Test
  public void muleAppWithReusableAppPlugin() throws Exception {
    final String appName = "mule-app-plugin-with-reusable-app";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertEquals(generatedAppClassLoaderModelFileContent, expectedAppClassLoaderModelFileContent, true);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Z_PATH_SUFFIX);
  }

  /**
   * Validates that additional libraries defined at the mule application level overrides all other additional libraries from mule
   * plugins.
   */
  @Test
  public void muleAppWithReusableAppPluginAndOverridedAdditionalDependency() throws Exception {
    final String appName = "mule-app-plugin-with-reusable-app-and-additional-dep-x-v2";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertEquals(generatedAppClassLoaderModelFileContent, expectedAppClassLoaderModelFileContent, true);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_V2_PATH_SUFFIX);
  }

  /**
   * Validates that if two mule-plugin have the same additional library then the latest one is going to be used.
   */
  @Test
  public void muleAppWithReusableAppAndUsingAnotherReusableAppWithANewestXVersion() throws Exception {
    final String appName = "mule-app-plugin-with-reusable-app-and-reusable-app2";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertEquals(generatedAppClassLoaderModelFileContent, expectedAppClassLoaderModelFileContent, true);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_V3_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Y_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Z_PATH_SUFFIX);
  }

  @Test
  public void muleAppWithPluginWithSameDependencyAsAdditionalDifferentClassifier() throws Exception {
    final String appName = "mule-app-plugin-with-same-additional-dep-different-classifier";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertEquals(generatedAppClassLoaderModelFileContent, expectedAppClassLoaderModelFileContent, true);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_X_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, GENERATED__DEPENDENCY_Y_PATH_SUFFIX);
    assertArtifactInstalled(appLocation, "/target/repository/group/id/y/artifact-id-y/1.0.0/artifact-id-y-1.0.0-test-jar.jar");
  }

  @Test
  public void muleAppWithPluginWithSameDependencyAsAdditionalDifferentClassifierAsTransitive() throws Exception {
    final String appName = "mule-app-plugin-with-same-additional-dep-different-classifier-as-transitive";
    final String appLocation = getAppLocation(appName);
    processSourcesOnProject(appLocation);
    String generatedAppClassLoaderModelFileContent = getFileContent(getCorrectGeneratedClassloaderModelPath(appLocation));
    String expectedAppClassLoaderModelFileContent =
        getFileContent(getCorrectExpectedClassloaderModelPath(appLocation, appName));
    assertEquals(generatedAppClassLoaderModelFileContent, expectedAppClassLoaderModelFileContent, true);
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
      assertThat(getFile(finalLocation).exists(), equalTo(true));
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  private String getCorrectExpectedClassloaderModelPath(String appLocation, String fileName) {
    return appLocation + "/expected-files/" + fileName + ".json";
  }

}
