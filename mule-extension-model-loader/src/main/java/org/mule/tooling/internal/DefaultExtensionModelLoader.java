package org.mule.tooling.internal;

import org.mule.maven.client.api.MavenClient;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.container.api.ModuleRepository;
import org.mule.runtime.container.internal.ClasspathModuleDiscoverer;
import org.mule.runtime.container.internal.CompositeModuleDiscoverer;
import org.mule.runtime.container.internal.ContainerClassLoaderFactory;
import org.mule.runtime.container.internal.DefaultModuleRepository;
import org.mule.runtime.container.internal.JreModuleDiscoverer;
import org.mule.runtime.container.internal.ModuleDiscoverer;
import org.mule.runtime.core.api.extension.MuleExtensionModelProvider;
import org.mule.runtime.deployment.model.api.artifact.extension.ExtensionModelDiscoverer;
import org.mule.runtime.extension.api.extension.XmlSdk1ExtensionModelProvider;
import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import org.mule.tooling.api.ExtensionModelLoader;

import com.mulesoft.mule.runtime.bti.api.extension.BtiExtensionModelProvider;
import com.mulesoft.mule.runtime.core.api.extension.MuleEeExtensionModelProvider;
import com.mulesoft.mule.runtime.http.policy.api.extension.HttpPolicyEeExtensionModelProvider;
import com.mulesoft.mule.runtime.module.batch.api.extension.BatchExtensionModelProvider;
import com.mulesoft.mule.runtime.module.serialization.kryo.api.extension.KryoSerializerEeExtensionModelProvider;
import com.mulesoft.mule.runtime.tracking.api.extension.TrackingEeExtensionModelProvider;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DefaultExtensionModelLoader implements ExtensionModelLoader {

  private final ExtensionModelDiscoverer extensionModelDiscoverer;
  private DefaultExtensionModelService service;
  private MuleVersion muleVersion;

  public DefaultExtensionModelLoader(MavenClient mavenClient, Path workingDir, ClassLoader parentClassloader,
                                     String runtimeVersion) {

    this.extensionModelDiscoverer = new ExtensionModelDiscoverer();
    this.muleVersion = new MuleVersion(runtimeVersion);
    
    List<ModuleDiscoverer> result = new ArrayList();
    result.add(new JreModuleDiscoverer());
    result.add(new ClasspathModuleDiscoverer(parentClassloader, workingDir.toFile()));
    final ModuleRepository moduleRepository =
        new DefaultModuleRepository(new CompositeModuleDiscoverer(result.toArray(new ModuleDiscoverer[0])));

    ArtifactClassLoader containerClassLoaderFactory =
        (new ContainerClassLoaderFactory(moduleRepository)).createContainerClassLoader(parentClassloader);
    MuleArtifactResourcesRegistry resourcesRegistry =
        new MuleArtifactResourcesRegistry(runtimeVersion, Optional.ofNullable(muleVersion), mavenClient,
                                          moduleRepository, containerClassLoaderFactory, workingDir.toFile());
    service = new DefaultExtensionModelService(resourcesRegistry);
  }

  @Override
  public Set<ExtensionModel> getRuntimeExtensionModels() {
    Set<ExtensionModel> runtimeExtensionModels = extensionModelDiscoverer.discoverRuntimeExtensionModels();
    runtimeExtensionModels.add(MuleExtensionModelProvider.getExtensionModel());
    runtimeExtensionModels.add(XmlSdk1ExtensionModelProvider.getExtensionModel());
    runtimeExtensionModels.add(MuleExtensionModelProvider.getTlsExtensionModel());
    runtimeExtensionModels.add(MuleEeExtensionModelProvider.getExtensionModel());
    runtimeExtensionModels.add(BatchExtensionModelProvider.getExtensionModel());
    runtimeExtensionModels.add(BtiExtensionModelProvider.getExtensionModel());
    runtimeExtensionModels.add(HttpPolicyEeExtensionModelProvider.getExtensionModel());
    runtimeExtensionModels.add(KryoSerializerEeExtensionModelProvider.getExtensionModel());
    runtimeExtensionModels.add(TrackingEeExtensionModelProvider.getExtensionModel());
    return runtimeExtensionModels;
  }

  @Override
  public PluginResources load(BundleDescriptor artifactDescriptor) {
    return service.loadExtensionData(artifactDescriptor, muleVersion);
  }
}
