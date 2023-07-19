/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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
