/*
 * Copyright (c) 2015 MuleSoft, Inc. This software is protected under international
 * copyright law. All use of this software is subject to MuleSoft's Master Subscription
 * Agreement (or other master license agreement) separately entered into in writing between
 * you and MuleSoft. If such an agreement is not in place, you may not use the software.
 */
package org.mule.tools.artifact.archiver.internal.type;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mule.tools.artifact.archiver.api.PackageBuilder;
import org.mule.tools.artifact.archiver.internal.packaging.type.BinariesType;
import org.mule.tools.artifact.archiver.internal.packaging.type.PackagingType;

import java.io.File;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.*;

public class BinariesTypeTest {

    private PackagingType packagingType = new BinariesType();

    @Test
    public void binariesStructureListDirectoriesTest() {
        assertThat("Directories set should not be null", packagingType.listDirectories(), notNullValue());
        assertThat("Directories set is not as expected", packagingType.listDirectories(),
                   containsInAnyOrder(PackageBuilder.MULE_FOLDER, PackageBuilder.CLASSES_FOLDER,
                                      PackageBuilder.REPOSITORY_FOLDER));
    }

    @Test
    public void binariesStructureListFilesTest() {
        assertThat("Files set should not be null", packagingType.listFiles(), notNullValue());
        assertThat("Files set should be empty", packagingType.listFiles(), empty());
    }

    @Test
    public void binariesTypeApplyPackagingTest() {

        PackageBuilder packageBuilderMock = mock(PackageBuilder.class);

        when(packageBuilderMock.withClasses(ArgumentMatchers.any())).thenReturn(packageBuilderMock);
        when(packageBuilderMock.withMule(ArgumentMatchers.any())).thenReturn(packageBuilderMock);

        Map<String, File> fileMapMock = mock(Map.class);

        packagingType.applyPackaging(packageBuilderMock, fileMapMock);

        verify(packageBuilderMock, times(1)).withClasses(ArgumentMatchers.any());
        verify(packageBuilderMock, times(1)).withMule(ArgumentMatchers.any());
        //        verify(packageBuilderMock, times(0)).withMetaInf(ArgumentMatchers.any());
    }

} 
