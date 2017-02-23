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

import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Map;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mule.tools.artifact.archiver.internal.PackageBuilder;
import org.mule.tools.artifact.archiver.internal.packaging.PackagingType;

import com.google.common.collect.ImmutableMap;

public class BinariesAndSourcesTypeTest extends PackageTypeTest {

  private PackagingType packagingType = PackagingType.BINARIES_AND_SOURCES;

  @Test
  public void validateApplyPackagingTest() {
    PackageBuilder packageBuilderMock = mock(PackageBuilder.class);
    File classes = mockFileWithName(PackageBuilder.CLASSES_FOLDER);
    File mule = mockFileWithName(PackageBuilder.MULE_FOLDER);

    when(packageBuilderMock.withClasses(ArgumentMatchers.any())).thenReturn(packageBuilderMock);
    when(packageBuilderMock.withMule(ArgumentMatchers.any())).thenReturn(packageBuilderMock);

    Map<String, File> fileMapMock = ImmutableMap.of(classes.getName(), classes, mule.getName(), mule);

    packagingType.applyPackaging(packageBuilderMock, fileMapMock);

    verify(packageBuilderMock).withClasses(classes);
    verify(packageBuilderMock).withMule(mule);
    // verify(packageBuilderMock, times(0)).withMetaInf(ArgumentMatchers.any());
  }



}
