package org.mule.tooling.internal;

import java.io.File;
import java.util.List;
import java.util.Optional;
import org.mule.maven.client.api.MavenClient;
import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.container.api.ModuleRepository;
import org.mule.runtime.deployment.model.api.builder.RegionPluginClassLoadersFactory;
import org.mule.runtime.deployment.model.api.plugin.ArtifactPluginClassLoaderFactory;
import org.mule.runtime.deployment.model.api.plugin.resolver.PluginDependenciesResolver;
import org.mule.runtime.deployment.model.internal.DefaultRegionPluginClassLoadersFactory;
import org.mule.runtime.module.artifact.api.classloader.ArtifactClassLoader;
import org.mule.runtime.module.artifact.api.classloader.DeployableArtifactClassLoaderFactory;
import org.mule.runtime.module.artifact.api.classloader.MuleDeployableArtifactClassLoader;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactDescriptor;
import org.mule.runtime.module.artifact.api.descriptor.ArtifactDescriptorValidatorBuilder;
import org.mule.runtime.module.artifact.api.descriptor.DescriptorLoaderRepository;
import org.mule.runtime.module.deployment.impl.internal.application.ApplicationDescriptorFactory;
import org.mule.runtime.module.deployment.impl.internal.domain.DomainDescriptorFactory;
import org.mule.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorFactory;
import org.mule.runtime.module.deployment.impl.internal.plugin.ArtifactPluginDescriptorLoader;
import org.mule.runtime.module.deployment.impl.internal.plugin.BundlePluginDependenciesResolver;

public class MuleArtifactResourcesRegistry {

  private String toolingVersion;
  private Optional<MuleVersion> targetMuleVersion;
  private MavenClient mavenClient;
  private ModuleRepository moduleRepository;
  private final File workingDirectory;
  private ArtifactPluginDescriptorFactory artifactPluginDescriptorFactory;
  private ArtifactPluginDescriptorLoader artifactPluginDescriptorLoader;
  private PluginDependenciesResolver pluginDependenciesResolver;
  private ArtifactClassLoader containerArtifactClassLoader;
  private ApplicationDescriptorFactory applicationDescriptorFactory;
  private DomainDescriptorFactory domainDescriptorFactory;
  private RegionPluginClassLoadersFactory regionPluginClassLoadersFactory;
  private DescriptorLoaderRepository descriptorLoaderRepository;

  public MuleArtifactResourcesRegistry(String toolingVersion, Optional<MuleVersion> targetMuleVersion, MavenClient mavenClient,
                                       ModuleRepository moduleRepository, ArtifactClassLoader containerArtifactClassLoader,
                                       File workingDirectory) {
    this.toolingVersion = toolingVersion;
    this.targetMuleVersion = targetMuleVersion;
    this.mavenClient = mavenClient;
    this.moduleRepository = moduleRepository;
    this.containerArtifactClassLoader = containerArtifactClassLoader;
    this.workingDirectory = workingDirectory;
    this.init();
  }

  public DescriptorLoaderRepository getDescriptorLoaderRepository() {
    return this.descriptorLoaderRepository;
  }

  private void init() {
    this.descriptorLoaderRepository = new ToolingDescriptorLoaderRepository(this.mavenClient);
    ArtifactDescriptorValidatorBuilder artifactDescriptorValidatorBuilder =
        ArtifactDescriptorValidatorBuilder.builder().validateMinMuleVersion().validateMinMuleVersion(() -> {
          return this.toolingVersion;
        }).validateMinMuleVersionUsingSemanticVersion();
    this.artifactPluginDescriptorFactory =
        new ArtifactPluginDescriptorFactory(this.descriptorLoaderRepository, artifactDescriptorValidatorBuilder);
    this.artifactPluginDescriptorLoader = new ArtifactPluginDescriptorLoader(this.artifactPluginDescriptorFactory);
    this.applicationDescriptorFactory =
        new ApplicationDescriptorFactory(this.artifactPluginDescriptorLoader, descriptorLoaderRepository,
                                         artifactDescriptorValidatorBuilder);
    this.domainDescriptorFactory =
        new DomainDescriptorFactory(this.artifactPluginDescriptorLoader, this.descriptorLoaderRepository,
                                    artifactDescriptorValidatorBuilder);
    this.regionPluginClassLoadersFactory =
        new DefaultRegionPluginClassLoadersFactory(new ArtifactPluginClassLoaderFactory(), this.moduleRepository);
    this.pluginDependenciesResolver = new BundlePluginDependenciesResolver(this.artifactPluginDescriptorFactory);
  }

  public Optional<MuleVersion> getTargetMuleVersion() {
    return this.targetMuleVersion;
  }

  public File getWorkingDirectory() {
    return this.workingDirectory;
  }

  public RegionPluginClassLoadersFactory getRegionPluginClassLoadersFactory() {
    return this.regionPluginClassLoadersFactory;
  }

  public DeployableArtifactClassLoaderFactory<ArtifactDescriptor> newTemporaryArtifactClassLoaderFactory() {
    return new MuleArtifactResourcesRegistry.TemporaryArtifactClassLoaderFactory();
  }

  public ArtifactClassLoader getContainerArtifactClassLoader() {
    return this.containerArtifactClassLoader;
  }

  public ApplicationDescriptorFactory getApplicationDescriptorFactory() {
    return this.applicationDescriptorFactory;
  }

  public DomainDescriptorFactory getDomainDescriptorFactory() {
    return this.domainDescriptorFactory;
  }

  public PluginDependenciesResolver getPluginDependenciesResolver() {
    return this.pluginDependenciesResolver;
  }

  public ArtifactPluginDescriptorLoader getArtifactPluginDescriptorLoader() {
    return this.artifactPluginDescriptorLoader;
  }


  private class TemporaryArtifactClassLoaderFactory implements DeployableArtifactClassLoaderFactory<ArtifactDescriptor> {

    private TemporaryArtifactClassLoaderFactory() {}

    public ArtifactClassLoader create(String artifactId, ArtifactClassLoader parent, ArtifactDescriptor descriptor,
                                      List<ArtifactClassLoader> artifactPluginClassLoaders) {
      return new MuleDeployableArtifactClassLoader(artifactId, descriptor, descriptor.getClassLoaderModel().getUrls(),
                                                   parent.getClassLoader(), parent.getClassLoaderLookupPolicy(),
                                                   artifactPluginClassLoaders);
    }
  }
}
