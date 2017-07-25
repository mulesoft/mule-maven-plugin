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

import org.mule.tools.api.PackageBuilder;

import com.google.common.collect.ImmutableMap;

public class BinariesAndSourcesTypeTest extends PackageTypeTest {

  private PackagingMode packagingMode = PackagingMode.BINARIES_AND_SOURCES;

  @Test
  public void validateApplyPackagingTest() {
    PackageBuilder packageBuilderMock = mock(PackageBuilder.class);
    File classes = mockFileWithName(PackagerFolders.CLASSES);
    File mule = mockFileWithName(PackagerFolders.MULE);

    when(packageBuilderMock.withClasses(ArgumentMatchers.any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withMule(ArgumentMatchers.any())).thenReturn(packageBuilderMock);

    Map<String, File> fileMapMock = ImmutableMap.of(classes.getName(), classes, mule.getName(), mule);

    packagingMode.applyPackaging(packageBuilderMock, fileMapMock);

    verify(packageBuilderMock).withClasses(classes);
    verify(packageBuilderMock).withMule(mule);
    // verify(packageBuilderMock, times(0)).withMetaInf(ArgumentMatchers.any());
  }



}
