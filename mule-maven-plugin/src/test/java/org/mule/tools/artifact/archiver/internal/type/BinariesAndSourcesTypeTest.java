/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.artifact.archiver.internal.type;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mule.tools.artifact.archiver.internal.PackageBuilder;
import org.mule.tools.artifact.archiver.internal.packaging.PackagingType;

import java.io.File;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.*;

public class BinariesAndSourcesTypeTest {

    private PackagingType packagingType = PackagingType.BINARIES_AND_SOURCES;

    @Test
    public void validateListDirectories() {
        assertThat("Directories set should not be null", packagingType.listDirectories(), notNullValue());
        assertThat("Directories set is not as expected", packagingType.listDirectories(), containsInAnyOrder(
            PackageBuilder.MULE_FOLDER, PackageBuilder.CLASSES_FOLDER, PackageBuilder.REPOSITORY_FOLDER, "META-INF"));
    }

    @Test
    public void validateApplyPackagingTest() {
        PackageBuilder packageBuilderMock = mock(PackageBuilder.class);

        when(packageBuilderMock.withClasses(ArgumentMatchers.any())).thenReturn(packageBuilderMock);
        when(packageBuilderMock.withMule(ArgumentMatchers.any())).thenReturn(packageBuilderMock);
        //        when(packageBuilderMock.withMetaInf(ArgumentMatchers.any())).thenReturn(packageBuilderMock);

        Map<String, File> fileMapMock = mock(Map.class);

        packagingType.applyPackaging(packageBuilderMock, fileMapMock);

        verify(packageBuilderMock).withClasses(ArgumentMatchers.any());
        verify(packageBuilderMock).withMule(ArgumentMatchers.any());
        //        verify(packageBuilderMock, times(1)).withMetaInf(ArgumentMatchers.any());
    }



}
