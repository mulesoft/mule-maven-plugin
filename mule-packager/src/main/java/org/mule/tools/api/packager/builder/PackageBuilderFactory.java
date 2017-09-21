/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.builder;

import static org.mule.tools.api.packager.packaging.PackagingType.MULE_DOMAIN_BUNDLE;

import org.mule.tools.api.packager.packaging.PackagingOptions;
import org.mule.tools.api.packager.packaging.PackagingType;

/**
 * Factory for package builders.
 */
public class PackageBuilderFactory {

  /**
   * @param packagingType packaging type of the package that is going to be built.
   * @param options packaging options. It can only be applied to packages that are not domain bundles.
   * @return
   */
  public static PackageBuilder create(PackagingType packagingType, PackagingOptions options) {
    if (packagingType.equals(MULE_DOMAIN_BUNDLE)) {
      return new DomainBundlePackageBuilder();
    }
    return new MulePackageBuilder().withPackagingOptions(options);
  }
}
