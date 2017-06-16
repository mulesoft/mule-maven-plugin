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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

public abstract class PackageTypeTest {


  File mockFileWithName(String name) {
    File mockFile = mock(File.class);
    when(mockFile.getName()).thenReturn(name);
    return mockFile;
  }
}
