/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class Utils {

  public static void copyResource(String resource, File target) throws Exception {
    FileUtils.copyToFile(Utils.class.getResourceAsStream("/" + resource), target);
  }
}
