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

import com.google.common.collect.ImmutableMap;
import org.mule.tools.api.packager.packaging.PackagingMode;
import org.mule.tools.api.packager.structure.PackagerFolders;

public class SourcesTypeTest extends PackageTypeTest {

  private PackagingMode packagingMode = PackagingMode.SOURCES;

  @Test
  public void applyPackagingTest() {
    PackageBuilder packageBuilderMock = mock(PackageBuilder.class);
    File muleSrc = mockFileWithName(PackagerFolders.MULE_SRC);

    when(packageBuilderMock.withMuleSrc(muleSrc)).thenReturn(packageBuilderMock);

    Map<String, File> fileMapMock = ImmutableMap.of(muleSrc.getName(), muleSrc);

    packagingMode.applyPackaging(packageBuilderMock, fileMapMock);

    verify(packageBuilderMock).withMuleSrc(muleSrc);
  }
}
