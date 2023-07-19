/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.packager.archiver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

import java.io.File;

import org.codehaus.plexus.archiver.dir.DirectoryArchiver;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.junit.Before;
import org.junit.Test;

public class DomainBundleExplodedArchiverTest {

  private DomainBundleArchiver archiver;

  @Before
  public void setUp() {
    archiver = new DomainBundleExplodedArchiver();
  }

  @Test
  public void validateArchiverType() {
    assertThat("The archiver type is not as expected", archiver.getArchiver(), instanceOf(DirectoryArchiver.class));
  }

}
