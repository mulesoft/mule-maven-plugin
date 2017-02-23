package org.mule.tools.artifact.archiver.internal.type;

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
