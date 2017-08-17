/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.validation;

import org.apache.commons.lang3.StringUtils;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.tools.api.classloader.model.ApplicationClassLoaderModelAssembler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mule.maven.client.internal.AetherMavenClient.MULE_PLUGIN_CLASSIFIER;

public class MulePluginDependencyResolutionValidator {

  /**
   * When method resolveMulePlugins is invoked, AetherMavenClient should resolve all mule plugins that are a direct or transitive
   * dependency of the application but that are not a mule-plugin transitive dependency. So now we check if all mule plugins that
   * were resolved as dependencies of other mule plugins were already been resolved by the AetherMavenClient. If they were not, it
   * means they are only a transitive dependency of mule plugins, and never a direct one.
   *
   * @param mulePluginDependencies mule plugins and its dependencies
   **/
  public static void verifyThatAreNoTransitiveMulePlugins(Map<BundleDependency, List<BundleDependency>> mulePluginDependencies)
      throws IllegalStateException {
    Set<BaseVersionBundleDescriptor> validMulePluginsDependencies =
        mulePluginDependencies.keySet().stream().map(BaseVersionBundleDescriptor::new)
            .collect(Collectors.toSet());
    Set<BaseVersionBundleDescriptor> mulePluginsResolvedAsMulePluginsDependencies =
        mulePluginDependencies.values().stream().flatMap(Collection::stream)
            .filter(bundleDependency -> bundleDependency.getDescriptor().getClassifier().isPresent())
            .filter(bundleDependency -> StringUtils.equals(MULE_PLUGIN_CLASSIFIER,
                                                           bundleDependency.getDescriptor().getClassifier().get()))
            .map(BaseVersionBundleDescriptor::new)
            .collect(Collectors.toSet());
    mulePluginsResolvedAsMulePluginsDependencies.removeAll(validMulePluginsDependencies);
    if (!mulePluginsResolvedAsMulePluginsDependencies.isEmpty()) {
      throw new IllegalStateException("The following mule plugins are TRANSITIVE dependencies of mule plugins but not a DIRECT dependencies of a mule plugin: "
          + mulePluginsResolvedAsMulePluginsDependencies);
    }
  }

  private static class BaseVersionBundleDescriptor {

    private final String groupId;
    private final String artifactId;
    private final String version;

    private BaseVersionBundleDescriptor(BundleDependency bundleDependency) {
      this.groupId = bundleDependency.getDescriptor().getGroupId();
      this.artifactId = bundleDependency.getDescriptor().getArtifactId();
      this.version = bundleDependency.getDescriptor().getBaseVersion();
    }

    public String getGroupId() {
      return groupId;
    }

    public String getArtifactId() {
      return artifactId;
    }

    public String getVersion() {
      return version;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      BaseVersionBundleDescriptor that = (BaseVersionBundleDescriptor) o;

      if (!getGroupId().equals(that.getGroupId())) {
        return false;
      }
      if (!getArtifactId().equals(that.getArtifactId())) {
        return false;
      }
      return getVersion().equals(that.getVersion());
    }

    @Override
    public int hashCode() {
      int result = getGroupId().hashCode();
      result = 31 * result + getArtifactId().hashCode();
      result = 31 * result + getVersion().hashCode();
      return result;
    }
  }
}
