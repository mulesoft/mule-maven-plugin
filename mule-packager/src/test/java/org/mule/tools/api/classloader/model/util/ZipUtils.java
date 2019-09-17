/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.classloader.model.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for compressing files using zip format.
 *
 * @since 1.0
 */
public final class ZipUtils {

  private ZipUtils() {}

  /**
   * Compress the content of the folder with all the resources to the target file.
   *
   * @param targetFile zip output {@link File}.
   * @param folder input {@link File} folder to read all the contents to include in zip file.
   */
  public static void compress(File targetFile, File folder) {
    ZipResource[] resources = getZipResources(folder);
    compress(targetFile, resources);
  }

  private static ZipResource[] getZipResources(File folder) {
    try {
      List<ZipResource> resources = new ArrayList<>();
      Files.walk(folder.toPath())
          .filter(Files::isRegularFile)
          .forEach(f -> resources.add(new ZipResource(f.toFile().getAbsolutePath(), getRelativePath(folder, f.toFile()))));
      return resources.toArray(new ZipResource[0]);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static String getRelativePath(File targetFolder, File file) {
    return file.getAbsolutePath().substring(targetFolder.getAbsolutePath().length() + 1);
  }

  /**
   * Describes a resource that can be compressed in a ZIP file
   */
  public static class ZipResource {

    private final String file;
    private final String alias;

    public ZipResource(String file, String alias) {
      this.file = file;
      this.alias = alias;
    }
  }

  private static void compress(File targetFile, ZipResource[] resources) {
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(targetFile))) {
      for (ZipResource zipResource : resources) {
        URI resourceUri;
        URL resourceUrl = ZipUtils.class.getClassLoader().getResource(zipResource.file);
        if (resourceUrl == null) {
          resourceUri = new File(zipResource.file).toURI();
        } else {
          resourceUri = resourceUrl.toURI();
        }

        try (FileInputStream in = new FileInputStream(new File(resourceUri))) {
          out.putNextEntry(new ZipEntry(zipResource.alias == null ? zipResource.file : zipResource.alias));

          byte[] buffer = new byte[1024];

          int count;
          while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
          }
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } catch (URISyntaxException e) {
      throw new RuntimeException("Error while compressing resources as zip file", e);
    }
  }


}
