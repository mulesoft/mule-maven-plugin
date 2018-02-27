/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.builder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.mule.tools.api.packager.packaging.PackagingOptions;

public class PackageBuilderFactoryTest {


  @Test
  public void createMulePackageBuilderMuleApplicationPackagingTypeTest() {
    PackageBuilder actualPackageBuilder =
        PackageBuilderFactory.create(mock(PackagingOptions.class));
    assertThat("Package builder type is not the expected", actualPackageBuilder, instanceOf(MulePackageBuilder.class));
  }
}
