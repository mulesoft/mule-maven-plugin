/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.util;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ZipArchiverTest {

  private ZipArchiver zipArchiver;
  private String fileName;
  private Path metaInfPath;

  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    fileName = temp.getRoot().getAbsolutePath() + File.separator + "test-file.zip";
    URI uri = getClass().getClassLoader().getResource("app-archiver/META-INF").toURI();
    String mainPath = Paths.get(uri).toString();
    metaInfPath = Paths.get(mainPath);
  }

  @Test
  public void toZipTest() throws Exception {
    zipArchiver = new ZipArchiver();
    zipArchiver.toZip(metaInfPath.toFile(), fileName);

    ZipFile zipFile = new ZipFile(fileName);
    assertThat(zipFile.size(), equalTo(3));
  }

  @Test
  public void toZipWithBlackListTest() throws Exception {
    String[] BLACK_LIST = new String[] {"mule_export.properties"};
    zipArchiver = new ZipArchiver(BLACK_LIST);
    zipArchiver.toZip(metaInfPath.toFile(), fileName);

    ZipFile zipFile = new ZipFile(fileName);
    assertThat(zipFile.size(), equalTo(2));
  }

}
