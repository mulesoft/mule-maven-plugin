package org.mule.tooling.internal.nativelib;

import org.mule.runtime.deployment.model.internal.nativelib.ArtifactCopyNativeLibraryFinder;
import org.mule.runtime.deployment.model.internal.nativelib.NativeLibraryFinder;
import org.mule.runtime.deployment.model.internal.nativelib.NativeLibraryFinderFactory;

import java.io.File;
import java.net.URL;

public class ToolingNativeLibraryFinderFactory implements NativeLibraryFinderFactory {

  private File workingDirectory;

  public ToolingNativeLibraryFinderFactory(File workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  @Override
  public NativeLibraryFinder create(String name, URL[] urls) {
    return new ArtifactCopyNativeLibraryFinder(new File(new File(workingDirectory, name), "temp"), urls);
  }
}
