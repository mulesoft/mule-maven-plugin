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
import org.mule.tools.artifact.archiver.internal.packaging.PackageStructureValidator;
import org.mule.tools.artifact.archiver.internal.packaging.PackagingTypeFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PackageStructureValidatorTest {
    @Test
    public void hasExpectedStructureTest() {
        PackageStructureValidator applicationPackageStructure = new PackageStructureValidator(PackagingTypeFactory.getDefaultPackaging());

        File[] defaultFileStructure = getDefaultFileStructure();

        assertThat("This file structure should match the expected structure", applicationPackageStructure.hasExpectedStructure(defaultFileStructure));

        List<File> wrongExpectedStructure = Arrays.asList(defaultFileStructure).subList(0,2);

        assertThat("This file structure should not match the expected structure:", not(applicationPackageStructure.hasExpectedStructure(wrongExpectedStructure.toArray(new File[0]))));
    }

    @Test
    public void hasExpectedStructureWithNullArgumentTest() {
        PackageStructureValidator applicationPackageStructure = new PackageStructureValidator(PackagingTypeFactory.getDefaultPackaging());

        assertThat("This file structure should match the expected structure", not(applicationPackageStructure.hasExpectedStructure(null)));
    }

    private File[] getDefaultFileStructure() {
        List<File> expectedStructure = new ArrayList<>();

        File classesFolderMock = mock(File.class);
        when(classesFolderMock.exists()).thenReturn(true);
        when(classesFolderMock.isDirectory()).thenReturn(true);
        when(classesFolderMock.getName()).thenReturn("classes");

        File muleFolderMock = mock(File.class);
        when(muleFolderMock.exists()).thenReturn(true);
        when(muleFolderMock.isDirectory()).thenReturn(true);
        when(muleFolderMock.getName()).thenReturn("mule");

        File repositoryFolderMock = mock(File.class);
        when(repositoryFolderMock.exists()).thenReturn(true);
        when(repositoryFolderMock.isDirectory()).thenReturn(true);
        when(repositoryFolderMock.getName()).thenReturn("repository");

        expectedStructure.add(classesFolderMock);
        expectedStructure.add(muleFolderMock);
        expectedStructure.add(repositoryFolderMock);
        return expectedStructure.toArray(new File[0]);
    }


}
