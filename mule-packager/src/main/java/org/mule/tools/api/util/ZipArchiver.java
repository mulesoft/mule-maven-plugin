/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Collections.EMPTY_SET;

/**
 * Class that generates a zip file containing all the resources in a given folder.
 */
public class ZipArchiver {

  private final Set<String> blackList;

  public ZipArchiver() {
    this.blackList = EMPTY_SET;
  }

  public ZipArchiver(String[] blackList) {
    this.blackList = new HashSet<>(Arrays.asList(blackList));
  }

  /**
   * Method that should be called to generate the zip.
   *
   * @param fileToZip  Path to directory or file to be zipped
   * @param outputFile Full path of output file
   * @throws IOException
   */
  public void toZip(File fileToZip, String outputFile) throws IOException {
    FileOutputStream fos = new FileOutputStream(outputFile);
    ZipOutputStream zipOut = new ZipOutputStream(fos);
    toZip(fileToZip, fileToZip.getName(), zipOut);
    zipOut.close();
    fos.close();
  }

  /**
   * Add a file or the contents of a directory (recursively) to a zip file.
   *
   * @param fileToZip
   * @param fileName
   * @param zipOut
   * @throws IOException
   */
  protected void toZip(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
    if (!isInBlackList(fileToZip)) {
      if (fileToZip.isDirectory()) {
        File[] children = fileToZip.listFiles();
        // Make sure that directory is added even when it is empty
        ZipEntry zipEntry = new ZipEntry(fileName + "/");
        zipOut.putNextEntry(zipEntry);
        if (children != null) {
          // Recurse on its children
          for (File childFile : children) {
            toZip(childFile, fileName + "/" + childFile.getName(), zipOut);
          }
        }
      } else {
        // If it is not a directory, add as a zip entry and stop recursing
        FileInputStream fis = new FileInputStream(fileToZip);
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
          zipOut.write(bytes, 0, length);
        }
        fis.close();
      }
    }
  }

  /**
   * Check whether file is in the black list
   *
   * @param fileToZip
   * @return boolean
   */
  private boolean isInBlackList(File fileToZip) {
    return blackList.contains(fileToZip.getName());
  }

}
