/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.api.packager.DomainBundleProjectFoldersGenerator;
import org.mule.tools.api.packager.packaging.PackagingType;

import java.io.IOException;
import java.nio.file.Path;

import static org.mule.tools.api.packager.PackagerTestUtils.*;

public class DomainBundleProjectFoldersGeneratorTest {

  protected static final String GROUP_ID = "org.mule.domainbundle";
  protected static final String ARTIFACT_ID = "domain-bundle-id";
  private static final String FAKE_DOMAIN_NAME = "mule-domain-1.0.0-mule-domain.jar";
  private static final String DOMAIN = "domain";
  private static final String APPLICATIONS = "applications";
  private static final String FAKE_APPLICATION_NAME_1 = "mule-app-a-1.0.0-mule-application.jar";
  private static final String FAKE_APPLICATION_NAME_2 = "mule-app-b-1.0.0-mule-application.jar";


  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();

  private Path basePath;

  private DomainBundleProjectFoldersGenerator generator;

  @Before
  public void setUp() {
    basePath = projectBaseFolder.getRoot().toPath();
  }

  @Test
  public void generateDomainFolder() throws IOException {
    Path domainBasePath = basePath.resolve(DOMAIN);

    generator = new DomainBundleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_DOMAIN_BUNDLE);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(domainBasePath);
  }

  @Test
  public void generateDomainFolderAlreadyPresent() throws IOException {
    Path domainBasePath = basePath.resolve(DOMAIN);
    createFolder(domainBasePath, FAKE_DOMAIN_NAME, true);

    generator = new DomainBundleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_DOMAIN_BUNDLE);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(domainBasePath);
    assertFileExists(domainBasePath.resolve(FAKE_DOMAIN_NAME));
  }

  @Test
  public void generateApplicationsFolder() throws IOException {
    Path domainBasePath = basePath.resolve(APPLICATIONS);

    generator = new DomainBundleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_DOMAIN_BUNDLE);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(domainBasePath);
  }

  @Test
  public void generateApplicationsFolderAlreadyPresent() throws IOException {
    Path applicationsBasePath = basePath.resolve(APPLICATIONS);
    createFolder(applicationsBasePath, FAKE_APPLICATION_NAME_1, true);
    createFolder(applicationsBasePath, FAKE_APPLICATION_NAME_2, true);

    generator = new DomainBundleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_DOMAIN_BUNDLE);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(applicationsBasePath);
    assertFileExists(applicationsBasePath.resolve(FAKE_APPLICATION_NAME_1));
    assertFileExists(applicationsBasePath.resolve(FAKE_APPLICATION_NAME_2));
  }

  @Test
  public void generateMetaInfFolder() throws IOException {
    Path metaInfBasePath = basePath.resolve(META_INF);

    generator = new DomainBundleProjectFoldersGenerator(GROUP_ID, ARTIFACT_ID, PackagingType.MULE_DOMAIN_BUNDLE);
    generator.generate(projectBaseFolder.getRoot().toPath());

    assertFolderExist(metaInfBasePath);
  }
}
