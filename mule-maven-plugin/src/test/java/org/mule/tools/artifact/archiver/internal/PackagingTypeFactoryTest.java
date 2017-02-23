/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.artifact.archiver.internal;

import org.junit.Test;
import org.mule.tools.artifact.archiver.internal.packaging.PackagingType;
import org.mule.tools.artifact.archiver.internal.packaging.PackagingTypeFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mule.tools.artifact.archiver.internal.packaging.PackagingType.*;

public class PackagingTypeFactoryTest {

  private static final String UNKNOWN_PACKAGING = "unknownPackaging";
  private static final String BINARIES_AND_SOURCES_NAME = "binariesAndSources";

  @Test
  public void getDefaultPackagingTest() {
    PackagingType actualPackagingType = PackagingTypeFactory.getDefaultPackaging();

    assertThat("Actual packaging type is not the expected", actualPackagingType, instanceOf(BINARIES.getDeclaringClass()));
  }

  @Test
  public void getPackagingTest() {
    PackagingType binariesAndSourcesPackagingType = PackagingTypeFactory.getPackaging(BINARIES_AND_SOURCES_NAME);
    PackagingType binariesPackagingType = PackagingTypeFactory.getPackaging(BINARIES.name());
    PackagingType sourcesPackagingType = PackagingTypeFactory.getPackaging(SOURCES.name());

    assertThat("Actual packaging type is not the expected", binariesAndSourcesPackagingType,
               instanceOf(BINARIES_AND_SOURCES.getDeclaringClass()));
    assertThat("Actual packaging type is not the expected", binariesPackagingType, instanceOf(BINARIES.getDeclaringClass()));
    assertThat("Actual packaging type is not the expected", sourcesPackagingType, instanceOf(SOURCES.getDeclaringClass()));
  }

  @Test
  public void getDefaultPackagingIfPackagingTypeIsUnknownTest() {
    PackagingType actualDefaultPackagingType = PackagingTypeFactory.getPackaging(UNKNOWN_PACKAGING);

    PackagingType expectedDefaultPackagingType = PackagingTypeFactory.getDefaultPackaging();

    assertThat("Actual packaging type is not the expected", actualDefaultPackagingType,
               instanceOf(expectedDefaultPackagingType.getClass()));
  }
}
