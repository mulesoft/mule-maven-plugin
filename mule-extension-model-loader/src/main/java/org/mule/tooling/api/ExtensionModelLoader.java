package org.mule.tooling.api;

import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.api.meta.model.ExtensionModel;

import java.util.Optional;
import java.util.Set;

public interface ExtensionModelLoader {

  Set<ExtensionModel> getRuntimeExtensionModels();

  Optional<LoadedExtensionInformation> load(BundleDescriptor artifactDescriptor);


}
