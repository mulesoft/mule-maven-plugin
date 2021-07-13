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
