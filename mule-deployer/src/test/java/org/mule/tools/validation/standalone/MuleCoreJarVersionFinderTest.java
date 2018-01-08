/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.validation.standalone;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class MuleCoreJarVersionFinderTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private File visitingFile;

  private MuleCoreJarVersionFinder finder;
  private BasicFileAttributes fileAttributesMock;
  private File temporaryFolderRoot;

  @Before
  public void setUp() throws IOException {
    temporaryFolder.create();
    finder = new MuleCoreJarVersionFinder();
    fileAttributesMock = mock(BasicFileAttributes.class);
    temporaryFolderRoot = temporaryFolder.getRoot();
  }

  @Test
  public void visitFileFindMuleCoreJarTest() throws IOException {
    String muleVersion = "4.0.0-SNAPSHOT";
    visitingFile = temporaryFolder.newFile("mule-core-" + muleVersion + ".jar");
    finder.visitFile(visitingFile.toPath(), fileAttributesMock);
    assertThat("Version was not correctly parsed from file name", finder.getMuleCoreVersion(), equalTo(muleVersion));
    assertOtherImplementedMethods(FileVisitResult.TERMINATE);
  }

  @Test
  public void visitFileFindAnyJarTest() throws IOException {
    visitingFile = temporaryFolder.newFile("any-jar.jar");
    finder.visitFile(visitingFile.toPath(), fileAttributesMock);
    assertThat("Version was not correctly parsed from file name", finder.getMuleCoreVersion(), equalTo(null));
    assertOtherImplementedMethods(FileVisitResult.CONTINUE);
  }


  private void assertOtherImplementedMethods(FileVisitResult terminate) throws IOException {
    assertThat("preVisitDirectory method does not return the expected FileVisitResult",
               finder.preVisitDirectory(temporaryFolderRoot.toPath(), fileAttributesMock), equalTo(terminate));
    assertThat("visitFileFailed method does not return the expected FileVisitResult",
               finder.visitFileFailed(visitingFile.toPath(), new IOException()), equalTo(terminate));
    assertThat("postVisitDirectory method does not return the expected FileVisitResult",
               finder.postVisitDirectory(temporaryFolderRoot.toPath(), new IOException()), equalTo(terminate));
  }
}
