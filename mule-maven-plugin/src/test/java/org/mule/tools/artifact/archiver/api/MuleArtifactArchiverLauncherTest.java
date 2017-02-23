/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.artifact.archiver.api;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mule.tools.artifact.archiver.internal.PackageBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class MuleArtifactArchiverLauncherTest {

  private static final String TARGET_OPTION = "--target";
  private static final String ABSOLUTE_PATH = "/tmp";
  private static final String UNKNOWN_OPTION = "--unknown";
  private static final String PACKAGE_OPTION = "--packageName";
  private static final String NAME = "app";
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

  @Before
  public void setUpStreams() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  @Test
  public void mainWithoutPackageNameArgumentTest() throws IOException {
    MuleArtifactArchiverLauncher launcher = new MuleArtifactArchiverLauncher();
    String[] args = new String[2];
    args[0] = TARGET_OPTION;
    args[1] = ABSOLUTE_PATH;
    launcher.main(args);
    assertThat("Printed message was not the expected", getHelpMessage(), equalTo(outContent.toString()));
  }

  @Test
  public void mainWithUnknownOptionTest() throws IOException {
    MuleArtifactArchiverLauncher launcher = new MuleArtifactArchiverLauncher();
    String[] args = new String[3];
    args[0] = UNKNOWN_OPTION;
    args[1] = PACKAGE_OPTION;
    args[2] = NAME;
    launcher.main(args);
    assertThat("Printed message was not the expected", getHelpMessage(), equalTo(outContent.toString()));
  }

  @Test
  public void mainMinimumOptionsTest() throws IOException {
    MuleArtifactArchiverLauncher launcher = new MuleArtifactArchiverLauncher();
    PackageBuilder packageBuilderMock = mock(PackageBuilder.class);
    launcher.setPackageBuilder(packageBuilderMock);
    String[] args = new String[2];
    args[0] = PACKAGE_OPTION;
    args[1] = NAME;
    launcher.main(args);
    verify(packageBuilderMock, times(1)).generateArtifact(ArgumentMatchers.any(), ArgumentMatchers.any());
  }

  private String getHelpMessage() {
    return "usage: muleArtifactArchiver [--artifactContent <content type>]\n" +
        "       --packageName <name> [--target <absolute path>]\n" +
        "Create package containing binaries and/or sources.\n" +
        "\n" +
        "    --artifactContent <content type>   define if package contains binaries\n" +
        "                                       and/or sources\n" +
        "    --packageName <name>               file name for generated file\n" +
        "    --target <absolute path>           path of source package structure\n" +
        "                                       root folder\n";
  }
}
