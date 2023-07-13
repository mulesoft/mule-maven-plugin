/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */


package org.mule.tools.api.packager.archiver;

import org.codehaus.plexus.archiver.dir.DirectoryArchiver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DomainBundleExplodedArchiverTest {

  private DomainBundleArchiver archiver;

  @BeforeEach
  public void setUp() {
    archiver = new DomainBundleExplodedArchiver();
  }

  @Test
  public void validateArchiverType() {
    assertThat(archiver.getArchiver()).describedAs("The archiver type is not as expected").isInstanceOf(DirectoryArchiver.class);
  }

}
