package org.mule.tooling.api;

import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.tooling.internal.PluginResources;

import java.io.File;
import java.util.List;

public interface ExtensionModelService {

  List<ExtensionModel> loadRuntimeExtensionModels();

  PluginResources loadExtensionData(File pluginJarFile);

  BundleDescriptor readBundleDescriptor(File pluginFile);

  PluginResources loadExtensionData(BundleDescriptor pluginDescriptor,
                                                                        MuleVersion muleVersion);
}
