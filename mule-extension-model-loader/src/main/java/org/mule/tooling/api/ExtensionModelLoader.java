package org.mule.tooling.api;

import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.tooling.internal.PluginResources;

import java.util.Set;

public interface ExtensionModelLoader {

  Set<ExtensionModel> getRuntimeExtensionModels();

  PluginResources load(BundleDescriptor artifactDescriptor);


}
