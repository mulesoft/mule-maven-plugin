/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import static integration.FileTreeMatcher.hasSameTreeStructure;
import static org.apache.commons.lang3.ArrayUtils.addAll;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.File;
import java.io.IOException;

import integration.ProjectFactory;
import org.apache.maven.shared.verifier.VerificationException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PackageMojoTest extends MojoTest implements SettingsConfigurator {

  private static final String PACKAGE = "package";
  private static final String TARGET_FOLDER_NAME = "target";

  public PackageMojoTest() {
    this.goal = PACKAGE;
  }

  @BeforeEach
  public void before() throws IOException, VerificationException {
    clearResources();
  }

  @Test
  public void testPackageApp() throws IOException, VerificationException {
    verifier.addCliArgument(PACKAGE);
    verifier.execute();

    File expectedStructure = getExpectedStructure();
    assertThat("The directory structure is different from the expected", targetFolder,
               hasSameTreeStructure(expectedStructure, excludes));
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testPackageAppUsingLightweightWithLocalRepository() throws IOException, VerificationException {
    String artifactId = "empty-lightweight-local-repository-classloader-model-project";
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliArgument("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliArgument("-DlightweightPackage=true");
    verifier.addCliArgument("-DuseLocalRepository=true");
    verifier.addCliArgument(PACKAGE);
    verifier.execute();

    File expectedStructure = getExpectedStructure("/expected-lightweight-local-repository-classloader-model-project-with-dwl");
    File targetStructure = new File(verifier.getBasedir() + File.separator + TARGET_FOLDER_NAME);

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure,
                                    addAll(excludes,
                                           "empty-lightweight-local-repository-classloader-model-project-1.0.0-SNAPSHOT-mule-application-light-package.jar",
                                           "temp", "munit-test.xml")));
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testPackageAppWithSharedLibraries() throws IOException, VerificationException {
    String artifactId = "validate-shared-libraries-project";
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliArgument("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliArgument("-X");
    verifier.addCliArgument(PACKAGE);
    verifier.execute();

    File expectedStructure = getExpectedStructure("/expected-package-app-shared-libraries");
    File targetStructure = new File(verifier.getBasedir() + File.separator + TARGET_FOLDER_NAME);

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, excludes));
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testPackagePolicy() throws IOException, VerificationException {
    String artifactId = "empty-package-policy-project";
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliArgument("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliArgument("-X");
    verifier.addCliArgument(PACKAGE);
    verifier.execute();

    File expectedStructure = getExpectedStructure("/expected-package-policy-structure");
    File targetStructure = new File(verifier.getBasedir() + File.separator + TARGET_FOLDER_NAME);

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, excludes));
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testPackageMultiModuleAppModuleCorrectStructure() throws IOException, VerificationException {
    testPackageAppConfigFiles();
    String artifactId = "multi-module-application";
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliArgument("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliArgument("-X");
    verifier.addCliArgument(PACKAGE);
    verifier.execute();

    String moduleName = "empty-app";
    File expectedStructure = getExpectedStructure("/multi-module-application" + File.separator + moduleName
        + File.separator + TARGET_FOLDER_NAME);
    File targetStructure = new File(verifier.getBasedir() + File.separator + moduleName + File.separator + TARGET_FOLDER_NAME);

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, excludes));
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testPackageMultiModulePolicyModuleCorrectStructure() throws IOException, VerificationException {
    testPackageAppConfigFiles();
    String artifactId = "multi-module-application";
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliArgument("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliArgument("-X");
    verifier.addCliArgument(PACKAGE);
    verifier.execute();

    String moduleName = "empty-policy";
    File expectedStructure = getExpectedStructure("/multi-module-application" + File.separator + moduleName
        + File.separator + TARGET_FOLDER_NAME);
    File targetStructure = new File(verifier.getBasedir() + File.separator + moduleName + File.separator + TARGET_FOLDER_NAME);

    assertThat("The directory structure is different from the expected", targetStructure,
               hasSameTreeStructure(expectedStructure, excludes));
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testPackageAppWithDefinedFinalName() throws IOException, VerificationException {
    String artifactId = "check-finalName-package-project";
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliArgument("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliArgument("-X");
    verifier.addCliArgument(PACKAGE);
    verifier.execute();

    assertThat("File exists", projectBaseDirectory.toPath().resolve("target/testApp-mule-application.jar").toFile().exists(),
               is(true));
    verifier.verifyErrorFreeLog();
  }

  @Test
  public void testPackageAppConfigFiles() throws IOException, VerificationException {
    String artifactId = "config-files-package-project";
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(artifactId, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.addCliArgument("-Dproject.basedir=" + projectBaseDirectory.getAbsolutePath());
    verifier.addCliArgument("-X");
    verifier.addCliArgument("-N");
    verifier.addCliArgument(INSTALL);
    verifier.addCliArgument(PACKAGE);
    verifier.execute();
    File sourceJar = new File(projectBaseDirectory, "target/config-files-package-project-1.0.0-SNAPSHOT-mule-application.jar");
    File destinationDirectory = new File(getFile("/expected-files"), "extracted-config-files-project-jar-content");
    destinationDirectory.mkdir();
    unpackJar(sourceJar, destinationDirectory);
    File expectedJarStructure = new File(getFile("/expected-files"), "expected-config-files-project-jar-content");

    assertThat("The directory structure is different from the expected", destinationDirectory,
               hasSameTreeStructure(expectedJarStructure, excludes));

    verifier.verifyErrorFreeLog();
  }

  private void unpackJar(File sourceFile, File destinationFolder) {
    final ZipUnArchiver zipUnArchiver = new ZipUnArchiver();
    zipUnArchiver.setSourceFile(sourceFile);
    zipUnArchiver.setDestDirectory(destinationFolder);
    zipUnArchiver.extract();
  }

}
