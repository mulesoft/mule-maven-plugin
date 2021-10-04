package org.mule.tooling.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.mule.maven.client.api.MavenClient;
import org.mule.runtime.core.api.config.bootstrap.ArtifactType;
import org.mule.runtime.core.api.registry.SpiServiceRegistry;
import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptorLoader;
import org.mule.runtime.module.artifact.api.descriptor.ClassLoaderModelLoader;
import org.mule.runtime.module.artifact.api.descriptor.DescriptorLoader;
import org.mule.runtime.module.artifact.api.descriptor.DescriptorLoaderRepository;
import org.mule.runtime.module.artifact.api.descriptor.LoaderNotFoundException;
import org.mule.runtime.module.deployment.impl.internal.application.DeployableMavenClassLoaderModelLoader;
import org.mule.runtime.module.deployment.impl.internal.plugin.PluginMavenClassLoaderModelLoader;
import org.mule.runtime.module.service.internal.artifact.LibFolderClassLoaderModelLoader;

public class ToolingDescriptorLoaderRepository implements DescriptorLoaderRepository {

  private Map<Class, List<DescriptorLoader>> descriptorLoaders = Maps.newHashMap();

  public ToolingDescriptorLoaderRepository(MavenClient mavenClient) {
    ToolingClassLoaderModelLoader toolingClassLoaderModelLoader =
        new ToolingClassLoaderModelLoader(Lists.newArrayList(new ClassLoaderModelLoader[] {
            new DeployableMavenClassLoaderModelLoader(Optional.of(mavenClient)), new PluginMavenClassLoaderModelLoader(Optional.of(mavenClient))}));
    this.descriptorLoaders.put(BundleDescriptorLoader.class,
                               this.findBundleDescriptorLoaders(BundleDescriptorLoader.class, new SpiServiceRegistry()));
    this.descriptorLoaders.put(ClassLoaderModelLoader.class,
                               Lists.newArrayList(new DescriptorLoader[] {toolingClassLoaderModelLoader}));
  }

  public ToolingDescriptorLoaderRepository() {
    ToolingClassLoaderModelLoader toolingClassLoaderModelLoader = new ToolingClassLoaderModelLoader(Lists
        .newArrayList(new ClassLoaderModelLoader[] {new LibFolderClassLoaderModelLoader()}));
    this.descriptorLoaders.put(BundleDescriptorLoader.class,
                               this.findBundleDescriptorLoaders(BundleDescriptorLoader.class, new SpiServiceRegistry()));
    this.descriptorLoaders.put(ClassLoaderModelLoader.class,
                               Lists.newArrayList(new DescriptorLoader[] {toolingClassLoaderModelLoader}));
  }

  private List<DescriptorLoader> findBundleDescriptorLoaders(Class<? extends DescriptorLoader> descriptorLoaderClass,
                                                             SpiServiceRegistry serviceRegistry) {
    List<DescriptorLoader> descriptorLoaders = new ArrayList();
    Collection<? extends DescriptorLoader> providers =
        serviceRegistry.lookupProviders(descriptorLoaderClass, this.getClass().getClassLoader());
    Iterator var5 = providers.iterator();

    while (var5.hasNext()) {
      DescriptorLoader loader = (DescriptorLoader) var5.next();
      descriptorLoaders.add(loader);
    }

    return descriptorLoaders;
  }

  public <T extends DescriptorLoader> T get(String id, ArtifactType artifactType, Class<T> loaderClass)
      throws LoaderNotFoundException {
    T descriptorLoader = null;
    List<T> registeredDescriptorLoaders = (List) this.descriptorLoaders.get(loaderClass);
    if (registeredDescriptorLoaders != null) {
      Iterator iterator = registeredDescriptorLoaders.iterator();

      while (iterator.hasNext()) {
        DescriptorLoader loader = (DescriptorLoader) iterator.next();
        if (loader.getId().equals(id) && loader.supportsArtifactType(artifactType)) {
          descriptorLoader = loaderClass.cast(loader);
        }
      }
    }

    if (descriptorLoader == null) {
      throw new LoaderNotFoundException(String.format("There is no loader with ID='%s' and type '%s'", id,
                                                      loaderClass.getName()));
    } else {
      return descriptorLoader;
    }
  }
}
