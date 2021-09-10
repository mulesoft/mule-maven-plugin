package org.mule.tooling.api;

import org.mule.runtime.api.meta.model.ExtensionModel;
import org.mule.runtime.api.util.LazyValue;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This class carries information about an extension that was loaded.
 *
 * @since 1.0
 */
public class LoadedExtensionInformation {

  private ExtensionModel extensionModel;
  private String minMuleVersion;

  public LoadedExtensionInformation(ExtensionModel extensionModel, String minMuleVersion) {
    this.extensionModel = extensionModel;
    this.minMuleVersion = minMuleVersion;
  }

  /**
   * @return the {@link ExtensionModel} associated to the extension that was loaded.
   */
  public ExtensionModel getExtensionModel() {
    return extensionModel;
  }

  /**
   * @return returns the min Mule Runtime that the plugin supports.
   */
  public String getMinMuleVersion() {
    return minMuleVersion;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
}
