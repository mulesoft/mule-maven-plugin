package org.mule.tooling.internal.domain;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;
import org.mule.runtime.deployment.model.api.domain.DomainDescriptor;
import org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor;
import org.mule.runtime.deployment.model.internal.RegionPluginClassLoadersFactory;
import org.mule.runtime.deployment.model.internal.plugin.PluginDependenciesResolver;
import org.mule.runtime.deployment.model.internal.tooling.ToolingArtifactClassLoader;
import org.mule.runtime.deployment.model.internal.tooling.ToolingDomainClassLoaderBuilder;
import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import org.mule.tooling.api.ToolingException;
import org.mule.tooling.internal.nativelib.ToolingNativeLibraryFinderFactory;

import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Factory that knows how to create a {@link ArtifactClassLoader} for domains.
 *
 * @since 4.1
 */
public class DomainClassLoaderFactory {

  private ArtifactClassLoader containerArtifactClassLoader;
  private RegionPluginClassLoadersFactory regionPluginClassLoadersFactory;
  private PluginDependenciesResolver pluginDependenciesResolver;

  public DomainClassLoaderFactory(ArtifactClassLoader containerArtifactClassLoader,
                                  RegionPluginClassLoadersFactory regionPluginClassLoadersFactory,
                                  PluginDependenciesResolver pluginDependenciesResolver) {
    requireNonNull(containerArtifactClassLoader, "containerArtifactClassLoader cannot be null");
    requireNonNull(regionPluginClassLoadersFactory, "regionPluginClassLoadersFactory cannot be null");
    requireNonNull(pluginDependenciesResolver, "pluginDependenciesResolver cannot be null");

    this.containerArtifactClassLoader = containerArtifactClassLoader;
    this.regionPluginClassLoadersFactory = regionPluginClassLoadersFactory;
    this.pluginDependenciesResolver = pluginDependenciesResolver;
  }

  public ToolingArtifactClassLoader createDomainClassLoader(DomainDescriptor domainDescriptor, File workingDirectory) {
    try {
      final ToolingDomainClassLoaderBuilder domainClassLoaderBuilder =
          new ToolingDomainClassLoaderBuilder(containerArtifactClassLoader,
                                              new org.mule.runtime.deployment.model.internal.domain.DomainClassLoaderFactory(
                                                                                                                             containerArtifactClassLoader
                                                                                                                                 .getClassLoader(),
                                                                                                                             new ToolingNativeLibraryFinderFactory(workingDirectory)),
                                              regionPluginClassLoadersFactory);
      domainClassLoaderBuilder.setArtifactDescriptor(domainDescriptor);

      List<ArtifactPluginDescriptor> resolvedArtifactPluginDescriptors =
          pluginDependenciesResolver
              .resolve(emptySet(),
                       ImmutableList.<ArtifactPluginDescriptor>builder().addAll(domainDescriptor.getPlugins()).build(), true);
      resolvedArtifactPluginDescriptors.stream().forEach(domainClassLoaderBuilder::addArtifactPluginDescriptors);

      return domainClassLoaderBuilder.build();
    } catch (IOException e) {
      throw new ToolingException("Error while creating domain class loader", e);
    }
  }

}
