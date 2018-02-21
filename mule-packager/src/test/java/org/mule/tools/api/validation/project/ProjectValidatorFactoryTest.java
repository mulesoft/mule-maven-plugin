/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.project;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.packager.packaging.PackagingType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProjectValidatorFactoryTest {

  private ProjectInformation projectInformationMock;

  @Before
  public void setUp() {
    projectInformationMock = mock(ProjectInformation.class);
  }

  @Test
  public void createMuleProjectValidatorMuleApplicationPackagingTest() {
    when(projectInformationMock.getPackaging()).thenReturn(PackagingType.MULE.toString());

    AbstractProjectValidator actualProjectValidator = ProjectValidatorFactory
        .create(projectInformationMock);

    assertThat("Project validator type is not the expected", actualProjectValidator, instanceOf(MuleProjectValidator.class));
  }

  @Test
  public void createMuleProjectValidatorMuleDomainPackagingTest() {
    when(projectInformationMock.getPackaging()).thenReturn(PackagingType.MULE_DOMAIN.toString());

    AbstractProjectValidator actualProjectValidator = ProjectValidatorFactory
        .create(projectInformationMock);

    assertThat("Project validator type is not the expected", actualProjectValidator, instanceOf(MuleProjectValidator.class));
  }
}
