/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager;

import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentMatchers;

import com.google.common.collect.ImmutableMap;
import org.mule.tools.api.packager.packaging.PackagingMode;
import org.mule.tools.api.packager.structure.PackagerFolders;

public class BinariesTypeTest extends PackageTypeTest {

  private PackagingMode packagingMode = PackagingMode.BINARIES;

  @Test
  public void binariesTypeApplyPackagingTest() {
    PackageBuilder packageBuilderMock = mock(PackageBuilder.class);
    File classes = mockFileWithName(PackagerFolders.CLASSES);
    File mule = mockFileWithName(PackagerFolders.MULE);
    File repository = mockFileWithName(PackagerFolders.REPOSITORY);

    when(packageBuilderMock.withClasses(ArgumentMatchers.any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withMule(ArgumentMatchers.any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withRepository(ArgumentMatchers.any())).thenReturn(packageBuilderMock);

    Map<String, File> fileMapMock =
        ImmutableMap.of(classes.getName(), classes, mule.getName(), mule, repository.getName(), repository);

    packagingMode.applyPackaging(packageBuilderMock, fileMapMock);

    verify(packageBuilderMock).withClasses(classes);
    verify(packageBuilderMock).withMule(mule);
    verify(packageBuilderMock).withRepository(repository);
  }

}
