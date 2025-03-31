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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mule.tools.client.standalone.exception.MuleControllerException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
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
    assertThatThrownBy(() -> controller.addConfProperty("newProperty"))
        .isInstanceOf(UncheckedIOException.class);

    controller.deploy(temporaryFolder.toPath().resolve("mule-app").toString());
    controller.addLibrary(temporaryFolder.toPath().resolve("lib.jar").toFile());
    controller.deployDomain(temporaryFolder.toPath().resolve("mule-app").toString());
    controller.deployDomain(temporaryFolder.toPath().resolve("conf").toFile().getAbsolutePath().toString());
    assertThatThrownBy(() -> controller.deployDomain(temporaryFolder.toPath().resolve("mule-invalid-app1").toString()))
        .isInstanceOf(MuleControllerException.class);
    File domainFileMock = Mockito.mock(File.class);
    Mockito.when(domainFileMock.exists()).thenReturn(false);
    Mockito.when(domainFileMock.getPath()).thenReturn("fake/domain/path");

    assertThatThrownBy(() -> controller.deployDomain(domainFileMock.getPath()))
        .isInstanceOf(MuleControllerException.class)
        .hasMessageContaining("Domain does not exist");
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
    String value = UUID.randomUUID().toString();
    ArgumentCaptor<String[]> args = ArgumentCaptor.forClass(String[].class);
    doNothing().when(osController).start(args.capture());
    controller.start(value);
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
    String value = UUID.randomUUID().toString();

    ArgumentCaptor<String[]> args = ArgumentCaptor.forClass(String[].class);
    doNothing().when(osController).start(args.capture());
    when(osController.status(any())).thenReturn(0);

    controller.start(value);
    controller.status(value);

    verify(osController, times(1)).status(any());
    assertThat(args.getValue()).hasSize(1).containsExactly(value);
  }

  @Test
  void getProcessIdTest() {
    String value = UUID.randomUUID().toString();

    ArgumentCaptor<String[]> args = ArgumentCaptor.forClass(String[].class);
    doNothing().when(osController).start(args.capture());
    when(osController.getProcessId()).thenReturn(0);

    controller.start(value);
    controller.getProcessId();

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

    assertThat(result).isEqualTo(0);
    assertThat(anchorFile)
        .as("Anchor file should have been deleted by deleteAnchors")
        .doesNotExist();
  }

  @Test
  void isRunningTest() {
    int IS_RUNNING_STATUS_CODE = 0;
    when(controller.status()).thenReturn(IS_RUNNING_STATUS_CODE);
    assertThat(controller.isRunning()).isTrue();
    when(controller.status()).thenReturn(999);
    assertThat(controller.isRunning()).isFalse();
  }

  @Test
  void getLogTest() throws IOException {
    File muleHome = temporaryFolder;
    File logsFolder = new File(muleHome, "logs");
    logsFolder.mkdir();

    File muleLog = new File(logsFolder, "mule.log");
    assertThat(muleLog.createNewFile()).isTrue();
    when(osController.getMuleHome()).thenReturn(muleHome.getAbsolutePath());
    File log = controller.getLog();
    assertThat(log.getAbsolutePath().toString()).contains(muleLog.getAbsolutePath().toString());
    muleLog.delete();

    File muleEELog = new File(logsFolder, "mule_ee.log");
    assertThat(muleEELog.createNewFile()).isTrue();
    when(osController.getMuleHome()).thenReturn(muleHome.getAbsolutePath());
    File log2 = controller.getLog();
    assertThat(log2.getAbsolutePath().toString()).contains(muleEELog.getAbsolutePath().toString());
    muleEELog.delete();

    when(osController.getMuleHome()).thenReturn(muleHome.getAbsolutePath());
    assertThatThrownBy(() -> controller.getLog()).isInstanceOf(MuleControllerException.class);
  }

  @Test
  void getLogWithAppNameTest() throws IOException {
    File muleHome = temporaryFolder;
    File logsFolder = new File(muleHome, "logs");
    logsFolder.mkdir();

    String appName = "testApp";
    File appLog = new File(logsFolder, "mule-app-" + appName + ".log");
    assertThat(appLog.createNewFile()).isTrue();
    when(osController.getMuleHome()).thenReturn(muleHome.getAbsolutePath());

    File log = controller.getLog(appName);
    assertThat(log.getAbsolutePath().toString()).contains(appLog.getAbsolutePath().toString());
    appLog.delete();

    String nonExistentAppName = "nonExistentApp";
    assertThatThrownBy(() -> controller.getLog(nonExistentAppName)).isInstanceOf(MuleControllerException.class);
  }

  @Test
  void installAndUninstallLicenseTest() {
    String licensePath = "/path/to/license.lic";

    when(osController.runSync(null, "--installLicense", licensePath, "-M-client")).thenReturn(0);
    assertThatCode(() -> controller.installLicense(licensePath)).doesNotThrowAnyException();

    when(osController.runSync(null, "--installLicense", licensePath, "-M-client")).thenReturn(1);

    assertThatThrownBy(() -> controller.installLicense(licensePath)).isInstanceOf(MuleControllerException.class);

    when(osController.runSync(null, "--unInstallLicense", "-M-client")).thenReturn(0);
    assertThatCode(() -> controller.uninstallLicense()).doesNotThrowAnyException();

    when(osController.runSync(null, "--unInstallLicense", "-M-client")).thenReturn(1);
    assertThatThrownBy(() -> controller.uninstallLicense()).isInstanceOf(MuleControllerException.class);
  }

  @Test
  public void undeployApplicationTest() throws IOException {
    temporaryFolder.toPath().resolve("apps").toFile().mkdirs();
    temporaryFolder.toPath().resolve("logs").toFile().mkdirs();

    String applicationName = "testApp";
    File appFile = temporaryFolder.toPath().resolve("apps").resolve(applicationName + ANCHOR_SUFFIX).toFile();
    assertThat(appFile.createNewFile()).isTrue();


    AbstractOSController abstractOSController = new WindowsController(temporaryFolder.getAbsolutePath(), 20000);
    Controller controller = new Controller(abstractOSController, temporaryFolder.getAbsolutePath());

    controller.undeploy(applicationName);

    assertThat(appFile.exists()).isFalse();
    assertThatThrownBy(() -> controller.undeploy(applicationName))
        .isInstanceOf(MuleControllerException.class)
        .hasMessageContaining("Couldn't undeploy application");
  }

  @Test
  public void undeployDomainTest() throws IOException {
    temporaryFolder.toPath().resolve("domains").toFile().mkdirs();

    String domainName = "testDomain";
    File domainFile = temporaryFolder.toPath().resolve("domains").resolve(domainName + ANCHOR_SUFFIX).toFile();
    assertThat(domainFile.createNewFile()).isTrue();

    AbstractOSController abstractOSController = new WindowsController(temporaryFolder.getAbsolutePath(), 20000);
    Controller controller = new Controller(abstractOSController, temporaryFolder.getAbsolutePath());


    controller.undeployDomain(domainName);
    assertThat(domainFile.exists()).isFalse();
    assertThatThrownBy(() -> controller.undeployDomain(domainName))
        .isInstanceOf(MuleControllerException.class)
        .hasMessageContaining("Couldn't undeploy domain");
    File nonDeletableFile = temporaryFolder.toPath().resolve("domains").resolve(domainName + ANCHOR_SUFFIX).toFile();
    nonDeletableFile.setReadable(false);
    nonDeletableFile.setWritable(false);
    assertThatThrownBy(() -> controller.undeployDomain(domainName))
        .isInstanceOf(MuleControllerException.class)
        .hasMessageContaining("Couldn't undeploy domain");
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 1})
  public void undeployAllTest(int index) throws IOException {
    temporaryFolder.toPath().resolve("apps").toFile().mkdirs();

    String applicationName = "testApp";
    File appFile = temporaryFolder.toPath().resolve("apps").resolve(applicationName + ANCHOR_SUFFIX).toFile();
    assertThat(appFile.createNewFile()).isTrue();

    AbstractOSController abstractOSController = new WindowsController(temporaryFolder.getAbsolutePath(), 20000);
    Controller controller = new Controller(abstractOSController, temporaryFolder.getAbsolutePath());
    if (index == 0) {
      controller.undeployAll();
      assertThat(temporaryFolder.toPath().resolve("apps").toFile().listFiles().length).isEqualTo(0);
    } else if (index == 1) {
      try (MockedStatic<FileUtils> mockedFileUtils = Mockito.mockStatic(FileUtils.class)) {
        mockedFileUtils.when(() -> FileUtils.forceDelete(Mockito.any())).thenThrow(new IOException("Forced delete failed"));

        assertThatThrownBy(() -> controller.undeployAll())
            .isInstanceOf(MuleControllerException.class)
            .hasMessageContaining("Could not delete directory");
      }
    }
  }

  @Test
  public void isDeployedTest() throws IOException {
    String appName = "testApp";
    File appFile = temporaryFolder.toPath().resolve("apps").resolve(appName + ANCHOR_SUFFIX).toFile();
    appFile.getParentFile().mkdirs();
    assertThat(appFile.createNewFile()).isTrue();

    assertThat(controller.isDeployed("nonExistentDomain")).isFalse();
  }

  @Test
  public void isDomainDeployedTest() throws IOException {
    String domainName = "testDomain";
    File domainFile = temporaryFolder.toPath().resolve("domains").resolve(domainName + ANCHOR_SUFFIX).toFile();
    domainFile.getParentFile().mkdirs();
    assertThat(domainFile.createNewFile()).isTrue();

    assertThat(controller.isDomainDeployed("nonExistentDomain")).isFalse();
  }

  @Test
  public void getArtifactInternalRepositoryTest() {
    String artifactName = "testArtifact";
    File artifactDir = temporaryFolder.toPath().resolve("apps").resolve(artifactName).toFile();
    artifactDir.mkdir();

    File repoDir = controller.getArtifactInternalRepository(artifactName);
    assertThat(repoDir).isNotNull();
  }

  @Test
  public void getRuntimeInternalRepositoryTest() {
    File repoDir = controller.getRuntimeInternalRepository();
    assertThat(repoDir).isNotNull();
  }

  @Test
  void RunSyncWithOutputStreamTest() {
    String command = "--someCommand";
    String[] args = {"arg1", "arg2"};
    OutputStream outputStream = new ByteArrayOutputStream();
    doReturn(0).when(osController).runSync(eq(command), eq(outputStream), eq(args));
    int result = osController.runSync(command, outputStream, args);

    assertThat(result).isEqualTo(0);
    verify(osController).runSync(eq(command), eq(outputStream), eq(args));
  }

}
