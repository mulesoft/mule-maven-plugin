package org.mule.tooling.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import org.mule.maven.client.api.MavenClient;
import org.mule.runtime.core.api.config.bootstrap.ArtifactType;
import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptorLoader;
import org.mule.runtime.module.artifact.api.descriptor.ClassLoaderConfigurationLoader;
import org.mule.runtime.module.artifact.api.descriptor.DescriptorLoader;
import org.mule.runtime.module.artifact.api.descriptor.DescriptorLoaderRepository;
import org.mule.runtime.module.artifact.api.descriptor.LoaderNotFoundException;
import org.mule.runtime.module.deployment.impl.internal.application.DeployableMavenClassLoaderConfigurationLoader;
import org.mule.runtime.module.deployment.impl.internal.plugin.PluginMavenClassLoaderConfigurationLoader;
import org.mule.runtime.module.service.internal.artifact.LibFolderClassLoaderConfigurationLoader;

import static com.google.common.collect.ImmutableList.copyOf;
import static java.util.Collections.emptyList;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ToolingDescriptorLoaderRepository implements DescriptorLoaderRepository {

  public static synchronized <T> Collection<T> doLookupProviders(Class<T> providerClass, ClassLoader classLoader) {
    Iterator<T> iterator = ServiceLoader.load(providerClass, classLoader).iterator();
    if (iterator.hasNext()) {
      return copyOf(iterator);
    } else {
      return emptyList();
    }
  }

  private final Map<Class, List<DescriptorLoader>> descriptorLoaders = Maps.newHashMap();

  public ToolingDescriptorLoaderRepository(MavenClient mavenClient) {
    ToolingClassLoaderConfigurationLoader toolingClassLoaderConfigurationLoader =
        new ToolingClassLoaderConfigurationLoader(Lists.newArrayList(new ClassLoaderConfigurationLoader[] {
            new DeployableMavenClassLoaderConfigurationLoader(Optional.of(mavenClient)),
            new PluginMavenClassLoaderConfigurationLoader(Optional.of(mavenClient))}));
    this.descriptorLoaders.put(BundleDescriptorLoader.class, this.findBundleDescriptorLoaders());
    this.descriptorLoaders.put(ClassLoaderConfigurationLoader.class,
                               Lists.newArrayList(new DescriptorLoader[] {toolingClassLoaderConfigurationLoader}));
  }

  public ToolingDescriptorLoaderRepository() {
    ToolingClassLoaderConfigurationLoader toolingClassLoaderConfigurationLoader = new ToolingClassLoaderConfigurationLoader(Lists
        .newArrayList(new ClassLoaderConfigurationLoader[] {new LibFolderClassLoaderConfigurationLoader()}));
    this.descriptorLoaders.put(BundleDescriptorLoader.class, this.findBundleDescriptorLoaders());
    this.descriptorLoaders.put(ClassLoaderConfigurationLoader.class,
                               Lists.newArrayList(new DescriptorLoader[] {toolingClassLoaderConfigurationLoader}));
  }

  private List<DescriptorLoader> findBundleDescriptorLoaders() {
    return new ArrayList<>(doLookupProviders(BundleDescriptorLoader.class, this.getClass().getClassLoader()));
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
