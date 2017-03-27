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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mule.tools.artifact.archiver.internal.packaging.PackagingMode.*;

import org.junit.Test;
import org.mule.tools.artifact.archiver.internal.packaging.PackagingMode;
import org.mule.tools.artifact.archiver.internal.packaging.PackagingModeFactory;

public class PackagingModeFactoryTest {

  private static final String UNKNOWN_PACKAGING = "unknownPackaging";
  private static final String BINARIES_AND_SOURCES_NAME = "binariesAndSources";

  @Test
  public void getDefaultPackagingTest() {
    PackagingMode actualPackagingMode = PackagingModeFactory.getDefaultPackaging();

    assertThat("Actual packaging type is not the expected", actualPackagingMode, instanceOf(BINARIES.getDeclaringClass()));
  }

  @Test
  public void getPackagingTest() {
    PackagingMode binariesAndSourcesPackagingMode = PackagingModeFactory.getPackaging(BINARIES_AND_SOURCES_NAME);
    PackagingMode binariesPackagingMode = PackagingModeFactory.getPackaging(BINARIES.name());
    PackagingMode sourcesPackagingMode = PackagingModeFactory.getPackaging(SOURCES.name());

    assertThat("Actual packaging type is not the expected", binariesAndSourcesPackagingMode,
               instanceOf(BINARIES_AND_SOURCES.getDeclaringClass()));
    assertThat("Actual packaging type is not the expected", binariesPackagingMode, instanceOf(BINARIES.getDeclaringClass()));
    assertThat("Actual packaging type is not the expected", sourcesPackagingMode, instanceOf(SOURCES.getDeclaringClass()));
  }

  @Test
  public void getDefaultPackagingIfPackagingTypeIsUnknownTest() {
    PackagingMode actualDefaultPackagingMode = PackagingModeFactory.getPackaging(UNKNOWN_PACKAGING);

    PackagingMode expectedDefaultPackagingMode = PackagingModeFactory.getDefaultPackaging();

    assertThat("Actual packaging type is not the expected", actualDefaultPackagingMode,
               instanceOf(expectedDefaultPackagingMode.getClass()));
  }
}
