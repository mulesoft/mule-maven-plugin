/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.standalone.controller;

import org.junit.jupiter.api.Test;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.ArgumentCaptor;

import org.mockito.MockedStatic;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class ControllerTest {

  protected static final String ANCHOR_SUFFIX = "-anchor.txt";

  @TempDir
  public File temporaryFolder;

  @Test
  public void testController() throws IOException {
    temporaryFolder.toPath().resolve("conf").toFile().mkdirs();
    temporaryFolder.toPath().resolve("lib").resolve("user").toFile().mkdirs();
    temporaryFolder.toPath().resolve("conf").resolve("wrapper.conf").toFile().createNewFile();
    temporaryFolder.toPath().resolve("mule-app").toFile().createNewFile();
    temporaryFolder.toPath().resolve("mule-invalid-app").toFile().createNewFile();
    temporaryFolder.toPath().resolve("lib.jar").toFile().createNewFile();
    BufferedWriter writer =
        new BufferedWriter(new FileWriter(temporaryFolder.toPath().resolve("conf").resolve("wrapper.conf").toFile()));
    writer.write("wrapper.java.additional.19=-Danypoint.platform.proxy_port=80");
    writer.close();
    AbstractOSController abstractOSController = new WindowsController(temporaryFolder.getAbsolutePath(), 20000);
    Controller controller = new Controller(abstractOSController, temporaryFolder.getAbsolutePath());
    controller.addConfProperty("2");
    Path muleInvalidAppPath = temporaryFolder.toPath().resolve("mule-invalid-app1");
    Files.createFile(muleInvalidAppPath);
    muleInvalidAppPath.toFile().setWritable(false);
    muleInvalidAppPath.toFile().setReadable(false);
    controller.wrapperConf = muleInvalidAppPath;
    assertThrows(UncheckedIOException.class, () -> controller.addConfProperty("newProperty"));

    controller.deploy(temporaryFolder.toPath().resolve("mule-app").toString());
    controller.addLibrary(temporaryFolder.toPath().resolve("lib.jar").toFile());
    controller.deployDomain(temporaryFolder.toPath().resolve("mule-app").toString());
    controller.deployDomain(temporaryFolder.toPath().resolve("conf").toFile().getAbsolutePath().toString());
  }

  private static class XController extends Controller {

    public XController(AbstractOSController osSpecificController, String muleHome) {
      super(osSpecificController, muleHome);
      this.libsDir = mock(File.class);
    }

    void resetController() {
      reset(this.libsDir);
    }
  }

  private final AbstractOSController osController = mock(AbstractOSController.class);
  private final XController controller = new XController(osController, UUID.randomUUID().toString());

  @BeforeEach
  void setUp() {
    reset(osController);
    controller.resetController();
  }

  @Test
  void getMuleBinTest() {
    String value = UUID.randomUUID().toString();

    when(osController.getMuleBin()).thenReturn(value);

    assertThat(controller.getMuleBin()).isEqualTo(value);
  }

  @Test
  void startTest() {
    // VALUES
    String value = UUID.randomUUID().toString();

    //CONFIG
    ArgumentCaptor<String[]> args = ArgumentCaptor.forClass(String[].class);
    doNothing().when(osController).start(args.capture());

    //EXEC
    controller.start(value);

    //VALIDATION
    verify(osController, times(1)).start(any());
    assertThat(args.getValue()).hasSize(1).containsExactly(value);
  }

  @Test
  void restartTest() {
    String value = UUID.randomUUID().toString();

    ArgumentCaptor<String[]> args = ArgumentCaptor.forClass(String[].class);
    doNothing().when(osController).restart(args.capture());

    controller.restart(value);

    verify(osController, times(1)).restart(any());
    assertThat(args.getValue()).hasSize(1).containsExactly(value);
  }

  @Test
  void statusTest() {
    // VALUES
    String value = UUID.randomUUID().toString();

    //CONFIG
    ArgumentCaptor<String[]> args = ArgumentCaptor.forClass(String[].class);
    doNothing().when(osController).start(args.capture());
    when(osController.status(any())).thenReturn(0);

    controller.start(value);
    controller.status(value);

    //VALIDATION
    verify(osController, times(1)).status(any());
    assertThat(args.getValue()).hasSize(1).containsExactly(value);
  }

  @Test
  void getProcessIdTest() {
    // VALUES
    String value = UUID.randomUUID().toString();

    //CONFIG
    ArgumentCaptor<String[]> args = ArgumentCaptor.forClass(String[].class);
    doNothing().when(osController).start(args.capture());
    when(osController.getProcessId()).thenReturn(0);

    controller.start(value);
    controller.getProcessId();

    //VALIDATION
    verify(osController, times(1)).getProcessId();
    assertThat(args.getValue()).hasSize(1).containsExactly(value);
  }

  @ParameterizedTest
  @MethodSource("addLibraryTestValues")
  void addLibraryTest(File jar, String message, boolean canWrite) {
    when(controller.libsDir.canWrite()).thenReturn(canWrite);

    if (!canWrite) {
      assertThatThrownBy(() -> controller.addLibrary(jar))
          .isInstanceOf(MuleControllerException.class)
          .hasMessageContaining(message);
    } else {
      try (MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class)) {
        fileUtils.when(() -> FileUtils.copyFileToDirectory(any(), any())).then(invocationOnMock -> null);
        controller.addLibrary(jar);

        fileUtils.verify(() -> FileUtils.copyFileToDirectory(any(), any()));

        fileUtils.when(() -> FileUtils.copyFileToDirectory(any(), any())).thenThrow(new IOException());
        assertThatThrownBy(() -> controller.addLibrary(jar))
            .isInstanceOf(MuleControllerException.class);
      }
    }
  }

  static Stream<Arguments> addLibraryTestValues() {
    File notFound = mock(File.class);
    when(notFound.exists()).thenReturn(false);

    File notExt = mock(File.class);
    when(notExt.exists()).thenReturn(true);
    when(notExt.getAbsolutePath()).thenReturn("anyName.txt");

    File readOnly = mock(File.class);
    when(readOnly.exists()).thenReturn(true);
    when(readOnly.getAbsolutePath()).thenReturn("library.jar");
    when(readOnly.canRead()).thenReturn(false);

    File jarFile = mock(File.class);
    when(jarFile.exists()).thenReturn(true);
    when(jarFile.getAbsolutePath()).thenReturn("library.jar");
    when(jarFile.canRead()).thenReturn(true);

    return Stream.of(
                     Arguments.of(notFound, "Jar file does not exist", false),
                     Arguments.of(notExt, "don't have .jar extension.", false),
                     Arguments.of(readOnly, "Cannot read jar file", false),
                     Arguments.of(jarFile, "Cannot write on lib dir", false),
                     Arguments.of(jarFile, null, true));
  }

  @Test
  void stopAnddeleteAnchorsTest() {
    temporaryFolder.toPath().resolve("conf").toFile();
    File arg2Dir = new File(temporaryFolder, "arg2");
    arg2Dir.mkdirs();

    File appsDir = new File(temporaryFolder, "apps");
    appsDir.mkdirs();
    File anchorFile = new File(temporaryFolder, "anchor.txt");
    XController controller = new XController(osController, temporaryFolder.getAbsolutePath());

    when(osController.stop(any(String[].class))).thenReturn(0);

    int result = controller.stop("arg1Dir", "arg2Dir");

    assertEquals(0, result);
    assertFalse(anchorFile.exists(), "Anchor file should have been deleted by deleteAnchors");
  }

  @Test
  void isRunningTest() {
    int IS_RUNNING_STATUS_CODE = 0;
    when(controller.status()).thenReturn(IS_RUNNING_STATUS_CODE);
    assertTrue(controller.isRunning());
    when(controller.status()).thenReturn(999);
    assertFalse(controller.isRunning());
  }

  @Test
  void getLogTest() throws IOException {
    File muleHome = temporaryFolder;
    File logsFolder = new File(muleHome, "logs");
    logsFolder.mkdir();

    File muleLog = new File(logsFolder, "mule.log");
    assertTrue(muleLog.createNewFile());
    when(osController.getMuleHome()).thenReturn(muleHome.getAbsolutePath());
    File log = controller.getLog();
    assertEquals(muleLog.getAbsolutePath(), log.getAbsolutePath());
    muleLog.delete();

    File muleEELog = new File(logsFolder, "mule_ee.log");
    assertTrue(muleEELog.createNewFile());
    when(osController.getMuleHome()).thenReturn(muleHome.getAbsolutePath());
    File log2 = controller.getLog();
    assertEquals(muleEELog.getAbsolutePath(), log2.getAbsolutePath());
    muleEELog.delete();


    when(osController.getMuleHome()).thenReturn(muleHome.getAbsolutePath());
    MuleControllerException exception = assertThrows(MuleControllerException.class, () -> {
      controller.getLog();
    });
    assertTrue(exception.getMessage().contains("There is no mule log available"));
  }

  @Test
  void getLogWithAppNameTest() throws IOException {
    File muleHome = temporaryFolder;
    File logsFolder = new File(muleHome, "logs");
    logsFolder.mkdir();

    // Case: Log file for the application exists
    String appName = "testApp";
    File appLog = new File(logsFolder, "mule-app-" + appName + ".log");
    assertTrue(appLog.createNewFile()); // Crear archivo de log
    when(osController.getMuleHome()).thenReturn(muleHome.getAbsolutePath());

    File log = controller.getLog(appName);
    assertEquals(appLog.getAbsolutePath(), log.getAbsolutePath());
    appLog.delete();

    // Case: Log file does not exist, exception is thrown
    String nonExistentAppName = "nonExistentApp";
    MuleControllerException exception = assertThrows(MuleControllerException.class, () -> {
      controller.getLog(nonExistentAppName);
    });
    assertTrue(exception.getMessage().contains(
                                               String.format("There is no app log available at %s/logs/mule-app-%s",
                                                             muleHome.getAbsolutePath(), nonExistentAppName)));
  }

  @Test
  void installAndUninstallLicenseTest() {
    String licensePath = "/path/to/license.lic";

    when(osController.runSync(null, "--installLicense", licensePath, "-M-client")).thenReturn(0);
    assertDoesNotThrow(() -> controller.installLicense(licensePath));

    when(osController.runSync(null, "--installLicense", licensePath, "-M-client")).thenReturn(1);
    MuleControllerException installException = assertThrows(MuleControllerException.class, () -> {
      controller.installLicense(licensePath);
    });
    assertEquals("Could not install license " + licensePath, installException.getMessage());

    when(osController.runSync(null, "--unInstallLicense", "-M-client")).thenReturn(0);
    assertDoesNotThrow(() -> controller.uninstallLicense());

    when(osController.runSync(null, "--unInstallLicense", "-M-client")).thenReturn(1);
    MuleControllerException uninstallException = assertThrows(MuleControllerException.class, () -> {
      controller.uninstallLicense();
    });
    assertEquals("Could not uninstall license", uninstallException.getMessage());
  }

  @Test
  public void undeployApplicationTest() throws IOException {
    temporaryFolder.toPath().resolve("apps").toFile().mkdirs();
    temporaryFolder.toPath().resolve("logs").toFile().mkdirs();

    String applicationName = "testApp";
    File appFile = temporaryFolder.toPath().resolve("apps").resolve(applicationName + ANCHOR_SUFFIX).toFile();
    assertTrue(appFile.createNewFile()); // Create the application file


    AbstractOSController abstractOSController = new WindowsController(temporaryFolder.getAbsolutePath(), 20000);
    Controller controller = new Controller(abstractOSController, temporaryFolder.getAbsolutePath());

    controller.undeploy(applicationName);

    assertFalse(appFile.exists(), "The application file should be deleted after undeploy.");

    MuleControllerException exception = assertThrows(MuleControllerException.class, () -> {
      controller.undeploy("nonExistentApp");
    });
    assertTrue(exception.getMessage().contains("Couldn't undeploy application"));
  }

  @Test
  public void undeployDomainTest() throws IOException {
    temporaryFolder.toPath().resolve("domains").toFile().mkdirs();

    String domainName = "testDomain";
    File domainFile = temporaryFolder.toPath().resolve("domains").resolve(domainName + ANCHOR_SUFFIX).toFile();
    assertTrue(domainFile.createNewFile());

    AbstractOSController abstractOSController = new WindowsController(temporaryFolder.getAbsolutePath(), 20000);
    Controller controller = new Controller(abstractOSController, temporaryFolder.getAbsolutePath());


    controller.undeployDomain(domainName);
    assertFalse(domainFile.exists(), "The domain file should be deleted after undeployDomain.");

    MuleControllerException exception = assertThrows(MuleControllerException.class, () -> {
      controller.undeployDomain("nonExistentDomain");
    });
    assertTrue(exception.getMessage().contains("Couldn't undeploy domain"));
    File nonDeletableFile = temporaryFolder.toPath().resolve("domains").resolve(domainName + ANCHOR_SUFFIX).toFile();
    nonDeletableFile.setReadable(false);
    nonDeletableFile.setWritable(false);

    MuleControllerException deleteException = assertThrows(MuleControllerException.class, () -> {
      controller.undeployDomain(domainName);
    });
    assertTrue(deleteException.getMessage().contains("Couldn't undeploy domain"));
  }

  @Test
  public void undeployAllTest() throws IOException {
    temporaryFolder.toPath().resolve("apps").toFile().mkdirs();

    String applicationName = "testApp";
    File appFile = temporaryFolder.toPath().resolve("apps").resolve(applicationName + ANCHOR_SUFFIX).toFile();
    assertTrue(appFile.createNewFile()); // Create the application file

    AbstractOSController abstractOSController = new WindowsController(temporaryFolder.getAbsolutePath(), 20000);
    Controller controller = new Controller(abstractOSController, temporaryFolder.getAbsolutePath());

    controller.undeployAll();
    assertEquals(0, temporaryFolder.toPath().resolve("apps").toFile().listFiles().length, "The apps directory should be empty.");
  }

  @Test
  public void isDeployedTest() throws IOException {
    String appName = "testApp";
    File appFile = temporaryFolder.toPath().resolve("apps").resolve(appName + ANCHOR_SUFFIX).toFile();
    appFile.getParentFile().mkdirs();
    assertTrue(appFile.createNewFile());

    assertFalse(controller.isDeployed("nonExistentApp"));
  }

  @Test
  public void isDomainDeployedTest() throws IOException {
    String domainName = "testDomain";
    File domainFile = temporaryFolder.toPath().resolve("domains").resolve(domainName + ANCHOR_SUFFIX).toFile();
    domainFile.getParentFile().mkdirs();
    assertTrue(domainFile.createNewFile());

    assertFalse(controller.isDomainDeployed("nonExistentDomain"));
  }

  @Test
  public void getArtifactInternalRepositoryTest() {
    String artifactName = "testArtifact";
    File artifactDir = temporaryFolder.toPath().resolve("apps").resolve(artifactName).toFile();
    artifactDir.mkdir();

    File repoDir = controller.getArtifactInternalRepository(artifactName);
    assertNotNull(repoDir);
  }

  @Test
  public void getRuntimeInternalRepositoryTest() {
    File repoDir = controller.getRuntimeInternalRepository();
    assertNotNull(repoDir);
  }

  @Test
  void RunSyncWithOutputStreamTest() {
    String command = "--someCommand";
    String[] args = {"arg1", "arg2"};
    OutputStream outputStream = new ByteArrayOutputStream();
    doReturn(0).when(osController).runSync(eq(command), eq(outputStream), eq(args));
    int result = osController.runSync(command, outputStream, args);

    assertEquals(0, result);
    verify(osController).runSync(eq(command), eq(outputStream), eq(args));
  }

}
