package org.mule.tooling.api;

import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface ExtensionModelService {

  List<ExtensionModel> loadRuntimeExtensionModels();

  Optional<LoadedExtensionInformation> loadExtensionData(File pluginJarFile);

  BundleDescriptor readBundleDescriptor(File pluginFile);

  Optional<LoadedExtensionInformation> loadExtensionData(BundleDescriptor pluginDescriptor);

  Optional<LoadedExtensionInformation> loadExtensionData(ArtifactClassLoader artifactClassLoader,
                                                         List<ArtifactClassLoader> artifactPluginClassLoaders);
}
