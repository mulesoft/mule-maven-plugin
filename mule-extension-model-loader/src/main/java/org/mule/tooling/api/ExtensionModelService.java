package org.mule.tooling.api;

import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.util.Pair;
import org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface ExtensionModelService {

  List<ExtensionModel> loadRuntimeExtensionModels();

  Set<Pair<ArtifactPluginDescriptor, ExtensionModel>> loadExtensionData(File pluginJarFile);

  BundleDescriptor readBundleDescriptor(File pluginFile);

  Set<Pair<ArtifactPluginDescriptor, ExtensionModel>> loadExtensionData(BundleDescriptor pluginDescriptor,
                                                                        MuleVersion muleVersion);
}
