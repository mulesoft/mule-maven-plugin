/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.builder;

import org.mule.tools.api.packager.packaging.PackagingOptions;

/**
 * Factory for package builders.
 */
public class PackageBuilderFactory {

  /**
   * @return
   * @param packagingOptions
   */
  public static PackageBuilder create(PackagingOptions packagingOptions) {
    return new MulePackageBuilder(packagingOptions);
  }
}
