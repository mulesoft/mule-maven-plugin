/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.mule.tools.api.packager.packaging.PackagingOptions;
import org.mule.tools.api.packager.packaging.PackagingType;

public class PackageBuilderFactoryTest {

  @Test
  public void createDomainBundlePackageBuilderTest() {
    PackageBuilder actualPackageBuilder =
        PackageBuilderFactory.create(PackagingType.MULE_DOMAIN_BUNDLE, mock(PackagingOptions.class));
    assertThat(actualPackageBuilder).describedAs("Package builder type is not the expected")
        .isInstanceOf(DomainBundlePackageBuilder.class);
  }

  @Test
  public void createMulePackageBuilderMuleApplicationPackagingTypeTest() {
    PackageBuilder actualPackageBuilder =
        PackageBuilderFactory.create(PackagingType.MULE_APPLICATION, mock(PackagingOptions.class));
    assertThat(actualPackageBuilder).describedAs("Package builder type is not the expected")
        .isInstanceOf(MulePackageBuilder.class);
  }

  @Test
  public void createMulePackageBuilderMuleDomainPackagingTypeTest() {
    PackageBuilder actualPackageBuilder =
        PackageBuilderFactory.create(PackagingType.MULE_DOMAIN, mock(PackagingOptions.class));
    assertThat(actualPackageBuilder).describedAs("Package builder type is not the expected")
        .isInstanceOf(MulePackageBuilder.class);
  }

  @Test
  public void createMulePackageBuilderMulePolicyPackagingTypeTest() {
    PackageBuilder actualPackageBuilder =
        PackageBuilderFactory.create(PackagingType.MULE_POLICY, mock(PackagingOptions.class));
    assertThat(actualPackageBuilder).describedAs("Package builder type is not the expected")
        .isInstanceOf(MulePackageBuilder.class);
  }
}
