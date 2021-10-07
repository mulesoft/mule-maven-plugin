package org.mule.tooling.api;

import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.util.Pair;
import org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor;

import java.util.Set;

public interface ExtensionModelLoader {

  Set<ExtensionModel> getRuntimeExtensionModels();

  Set<Pair<ArtifactPluginDescriptor, ExtensionModel>> load(BundleDescriptor artifactDescriptor);


}
