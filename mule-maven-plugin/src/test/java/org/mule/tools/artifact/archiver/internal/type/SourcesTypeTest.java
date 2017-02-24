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
import org.mule.tools.artifact.archiver.api.PackagerFolders;
import org.mule.tools.artifact.archiver.internal.PackageBuilder;
import org.mule.tools.artifact.archiver.internal.packaging.PackagingType;

import com.google.common.collect.ImmutableMap;

public class SourcesTypeTest extends PackageTypeTest {

  private PackagingType packagingType = PackagingType.SOURCES;

  @Test
  public void applyPackagingTest() {
    PackageBuilder packageBuilderMock = mock(PackageBuilder.class);
    File muleSrc = mockFileWithName(PackagerFolders.MULE_SRC);

    when(packageBuilderMock.withMuleSrc(muleSrc)).thenReturn(packageBuilderMock);

    Map<String, File> fileMapMock = ImmutableMap.of(muleSrc.getName(), muleSrc);

    packagingType.applyPackaging(packageBuilderMock, fileMapMock);

    verify(packageBuilderMock).withMuleSrc(muleSrc);
  }
}
