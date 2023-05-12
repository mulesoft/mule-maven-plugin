/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.api;

import org.mule.runtime.api.meta.model.ExtensionModel;

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
