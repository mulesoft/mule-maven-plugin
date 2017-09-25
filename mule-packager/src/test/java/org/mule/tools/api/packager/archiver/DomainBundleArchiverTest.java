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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;

import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.junit.Before;
import org.junit.Test;

public class DomainBundleArchiverTest {

  private static final String APPLICATIONS_LOCATION = "applications" + File.separator;
  private static final String DOMAIN_LOCATION = "domain" + File.separator;

  private File fileMock;

  private DomainBundleArchiver archiver;
  private DomainBundleArchiver archiverSpy;

  @Before
  public void setUp() {
    archiver = new DomainBundleArchiver();
    archiverSpy = spy(archiver);
    doNothing().when(archiverSpy).addResource(any(), any(), any(), any());
    fileMock = mock(File.class);
  }

  @Test
  public void validateArchiverType() {
    assertThat("The archiver type is not as expected", archiver.getArchiver(), instanceOf(ZipArchiver.class));
  }

  @Test
  public void addApplicationsTest() {
    archiverSpy.addApplications(fileMock, null, null);
    verify(archiverSpy, times(1)).addResource(APPLICATIONS_LOCATION, fileMock, null, null);
  }

  @Test
  public void addDomainTest() {
    archiverSpy.addDomain(fileMock, null, null);
    verify(archiverSpy, times(1)).addResource(DOMAIN_LOCATION, fileMock, null, null);
  }
}
