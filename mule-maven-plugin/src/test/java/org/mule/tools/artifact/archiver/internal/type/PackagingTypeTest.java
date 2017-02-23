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
import org.mule.tools.artifact.archiver.api.PackageBuilder;
import org.mule.tools.artifact.archiver.internal.packaging.type.BinariesAndSourcesType;
import org.mule.tools.artifact.archiver.internal.packaging.type.BinariesType;
import org.mule.tools.artifact.archiver.internal.packaging.type.PackagingType;
import org.mule.tools.artifact.archiver.internal.packaging.type.SourcesType;

import java.io.File;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.*;

public class PackagingTypeTest {

    public static PackagingType packagingType;

    @Test
    public void binariesAndSourceTypeApplyPackagingTest() {
        packagingType = new BinariesAndSourcesType();

        PackageBuilder packageBuilderMock = mock(PackageBuilder.class);

        when(packageBuilderMock.withClasses(ArgumentMatchers.any())).thenReturn(packageBuilderMock);
        when(packageBuilderMock.withMule(ArgumentMatchers.any())).thenReturn(packageBuilderMock);
//        when(packageBuilderMock.withMetaInf(ArgumentMatchers.any())).thenReturn(packageBuilderMock);

        Map<String, File> fileMapMock = mock(Map.class);

        packagingType.applyPackaging(packageBuilderMock, fileMapMock);

        verify(packageBuilderMock, times(1)).withClasses(ArgumentMatchers.any());
        verify(packageBuilderMock, times(1)).withMule(ArgumentMatchers.any());
//        verify(packageBuilderMock, times(1)).withMetaInf(ArgumentMatchers.any());
    }

    @Test
    public void binariesTypeApplyPackagingTest() {
        packagingType = new BinariesType();

        PackageBuilder packageBuilderMock = mock(PackageBuilder.class);

        when(packageBuilderMock.withClasses(ArgumentMatchers.any())).thenReturn(packageBuilderMock);
        when(packageBuilderMock.withMule(ArgumentMatchers.any())).thenReturn(packageBuilderMock);

        Map<String, File> fileMapMock = mock(Map.class);

        packagingType.applyPackaging(packageBuilderMock, fileMapMock);

        verify(packageBuilderMock, times(1)).withClasses(ArgumentMatchers.any());
        verify(packageBuilderMock, times(1)).withMule(ArgumentMatchers.any());
//        verify(packageBuilderMock, times(0)).withMetaInf(ArgumentMatchers.any());
    }

    @Test
    public void sourceTypeApplyPackagingTest() {
        packagingType = new SourcesType();

        PackageBuilder packageBuilderMock = mock(PackageBuilder.class);

//        when(packageBuilderMock.withMetaInf(ArgumentMatchers.any())).thenReturn(packageBuilderMock);

        Map<String, File> fileMapMock = mock(Map.class);

        packagingType.applyPackaging(packageBuilderMock, fileMapMock);

        verify(packageBuilderMock, times(0)).withClasses(ArgumentMatchers.any());
        verify(packageBuilderMock, times(0)).withMule(ArgumentMatchers.any());
//        verify(packageBuilderMock, times(1)).withMetaInf(ArgumentMatchers.any());
    }

    @Test
    public void binariesAndSourcesStructureListDirectoriesTest() {
        packagingType = new BinariesAndSourcesType();

        Set<String> actualDirectories = packagingType.listDirectories();

        assertThat("Set of directories should not be null", actualDirectories, notNullValue());
        // TODO we should actualy validate meta-inf/mule-src
        assertThat("The expected directories does not match the actual directories", actualDirectories, containsInAnyOrder(
            PackageBuilder.MULE_FOLDER, PackageBuilder.CLASSES_FOLDER, PackageBuilder.REPOSITORY_FOLDER, "META-INF"));
    }

    @Test
    public void binariesAndSourcesStructureListFilesTest() {
        packagingType = new BinariesAndSourcesType();

        Set<String> actualFiles = packagingType.listFiles();

        assertThat("List of files should not be null", actualFiles, notNullValue());
        assertThat("The expected files should be empty", actualFiles, empty());
    }

    @Test
    public void binariesStructureListDirectoriesTest() {
        packagingType = new BinariesType();

        Set<String> actualDirectories = packagingType.listDirectories();

        assertThat("Set of directories should not be null", actualDirectories, notNullValue());
        assertThat("The expected directories does not match the actual directories", actualDirectories, containsInAnyOrder(
            PackageBuilder.MULE_FOLDER, PackageBuilder.CLASSES_FOLDER, PackageBuilder.REPOSITORY_FOLDER));
    }

    @Test
    public void binariesStructureListFilesTest() {
        packagingType = new BinariesType();

        Set<String> actualFiles = packagingType.listFiles();

        assertThat("List of files should not be null", actualFiles, notNullValue());
        assertThat("The expected files should be empty", actualFiles, empty());
    }

    @Test
    public void sourcesStructureListDirectoriesTest() {
        packagingType = new SourcesType();

        Set<String> actualDirectories = packagingType.listDirectories();

        assertThat("Set of directories should not be null", actualDirectories, notNullValue());
        // TODO we should actualy validate meta-inf/mule-src
        assertThat("The expected directories does not match the actual directories", actualDirectories,
                   containsInAnyOrder("META-INF"));
    }

    @Test
    public void sourcesStructureListFilesTest() {
        packagingType = new SourcesType();

        Set<String> actualFiles = packagingType.listFiles();

        assertThat("List of files should not be null", actualFiles, notNullValue());
        assertThat("The expected files are not empty", actualFiles, empty());
    }
}
