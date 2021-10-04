package org.mule.tooling.internal.application;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import org.mule.runtime.deployment.model.api.application.ApplicationDescriptor;
import org.mule.runtime.deployment.model.api.builder.RegionPluginClassLoadersFactory;
import org.mule.runtime.deployment.model.api.plugin.ArtifactPluginDescriptor;
import org.mule.runtime.deployment.model.api.plugin.resolver.PluginDependenciesResolver;
import org.mule.runtime.deployment.model.internal.application.MuleApplicationClassLoaderFactory;
import org.mule.runtime.deployment.model.internal.tooling.ToolingApplicationClassLoaderBuilder;
import org.mule.runtime.deployment.model.internal.tooling.ToolingArtifactClassLoader;
import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import org.mule.runtime.module.artifact.api.descriptor.BundleDescriptor;
import org.mule.tooling.api.ToolingException;
import org.mule.tooling.internal.nativelib.ToolingNativeLibraryFinderFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Factory that knows how to create a {@link ArtifactClassLoader} for applications.
 *
 * @since 4.0
 */
public class ApplicationClassLoaderFactory {

  private ArtifactClassLoader containerArtifactClassLoader;
  private RegionPluginClassLoadersFactory regionPluginClassLoadersFactory;
  private PluginDependenciesResolver pluginDependenciesResolver;

  public ApplicationClassLoaderFactory(ArtifactClassLoader containerArtifactClassLoader,
                                       RegionPluginClassLoadersFactory regionPluginClassLoadersFactory,
                                       PluginDependenciesResolver pluginDependenciesResolver) {
    requireNonNull(containerArtifactClassLoader, "containerArtifactClassLoader cannot be null");
    requireNonNull(regionPluginClassLoadersFactory, "regionPluginClassLoadersFactory cannot be null");
    requireNonNull(pluginDependenciesResolver, "pluginDependenciesResolver cannot be null");

    this.containerArtifactClassLoader = containerArtifactClassLoader;
    this.regionPluginClassLoadersFactory = regionPluginClassLoadersFactory;
    this.pluginDependenciesResolver = pluginDependenciesResolver;
  }

  public ToolingArtifactClassLoader createApplicationClassLoader(ApplicationDescriptor applicationDescriptor,
                                                                 File workingDirectory,
                                                                 ToolingArtifactClassLoader parentToolingArtifactClassLoader) {
    try {
      ToolingApplicationClassLoaderBuilder builder =
          new ToolingApplicationClassLoaderBuilder(new MuleApplicationClassLoaderFactory(new ToolingNativeLibraryFinderFactory(workingDirectory)),
                                                   regionPluginClassLoadersFactory);

      Set<ArtifactPluginDescriptor> domainPluginDescriptors = new HashSet<>();
      boolean hasDomainDependency = applicationDescriptor.getDomainDescriptor().isPresent();
      if (!hasDomainDependency) {
        builder.setParentClassLoader(containerArtifactClassLoader);
      } else {
        final BundleDescriptor declaredDomainDescriptor = applicationDescriptor.getDomainDescriptor().get();
        if (hasDomainDependency && parentToolingArtifactClassLoader == null) {
          throw new IllegalStateException(format("Application '%s' declares a domain dependency '%s' that should have been already resolved",
                                                 applicationDescriptor.getArtifactDeclaration().getName(),
                                                 declaredDomainDescriptor));
        }

        for (ArtifactClassLoader artifactPluginClassLoader : parentToolingArtifactClassLoader.getArtifactPluginClassLoaders()) {
          domainPluginDescriptors.add(artifactPluginClassLoader.getArtifactDescriptor());
        }

        builder.setDomainParentClassLoader(parentToolingArtifactClassLoader.getRegionClassLoader());
      }

      pluginDependenciesResolver.resolve(domainPluginDescriptors, new ArrayList<>(applicationDescriptor.getPlugins()), true)
          .stream()
          .forEach(builder::addArtifactPluginDescriptors);

      builder.setArtifactDescriptor(applicationDescriptor);

      return builder.build();
    } catch (Exception e) {
      throw new ToolingException("Error while creating application class loader", e);
    }
  }
}
