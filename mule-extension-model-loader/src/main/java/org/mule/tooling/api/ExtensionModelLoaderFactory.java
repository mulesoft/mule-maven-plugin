/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.api;

import org.mule.maven.client.api.MavenClient;
import org.mule.tooling.internal.DefaultExtensionModelLoader;

import java.nio.file.Path;

public class ExtensionModelLoaderFactory {

  public static ExtensionModelLoader createLoader(MavenClient mavenClient, Path workingDir, ClassLoader parentClassloader,
                                                  String toolingVersion) {
    return new DefaultExtensionModelLoader(mavenClient, workingDir, parentClassloader, toolingVersion);
  }
}
