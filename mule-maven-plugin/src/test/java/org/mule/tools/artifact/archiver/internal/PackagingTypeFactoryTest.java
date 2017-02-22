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
import org.mule.tools.artifact.archiver.internal.packaging.PackagingTypeFactory;
import org.mule.tools.artifact.archiver.internal.packaging.type.BinariesAndSourcesType;
import org.mule.tools.artifact.archiver.internal.packaging.type.BinariesType;
import org.mule.tools.artifact.archiver.internal.packaging.type.PackagingType;
import org.mule.tools.artifact.archiver.internal.packaging.type.SourcesType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

public class PackagingTypeFactoryTest {

    private static final String UNKNOWN_PACKAGING = "unknownPackaging";

    @Test
    public void getDefaultPackagingTest() {
        PackagingType actualPackagingType = PackagingTypeFactory.getDefaultPackaging();

        assertThat("Actual packaging type is not the expected", actualPackagingType, instanceOf(BinariesType.class));
    }

    @Test
    public void getPackagingTest() {
        PackagingType binariesAndSourcesPackagingType = PackagingTypeFactory.getPackaging(PackagingTypeFactory.BINARIES_AND_SOURCES_PACKAGING);
        PackagingType binariesPackagingType = PackagingTypeFactory.getPackaging(PackagingTypeFactory.BINARIES_PACKAGING);
        PackagingType sourcesPackagingType = PackagingTypeFactory.getPackaging(PackagingTypeFactory.SOURCES_PACKAGING);

        assertThat("Actual packaging type is not the expected", binariesAndSourcesPackagingType, instanceOf(BinariesAndSourcesType.class));
        assertThat("Actual packaging type is not the expected", binariesPackagingType, instanceOf(BinariesType.class));
        assertThat("Actual packaging type is not the expected", sourcesPackagingType, instanceOf(SourcesType.class));
    }

    @Test
    public void getDefaultPackagingIfPackagingTypeIsUnknownTest() {
        PackagingType actualDefaultPackagingType = PackagingTypeFactory.getPackaging(UNKNOWN_PACKAGING);

        PackagingType expectedDefaultPackagingType = PackagingTypeFactory.getDefaultPackaging();

        assertThat("Actual packaging type is not the expected", actualDefaultPackagingType, instanceOf(expectedDefaultPackagingType.getClass()));
    }
}
