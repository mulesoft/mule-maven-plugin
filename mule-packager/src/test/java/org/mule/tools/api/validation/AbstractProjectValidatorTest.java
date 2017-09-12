/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import org.junit.rules.ExpectedException;
import org.mule.tools.api.exception.ValidationException;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mule.tools.api.util.Project;

public class AbstractProjectValidatorTest {

  protected static final String GROUP_ID = "group-id";
  protected static final String ARTIFACT_ID = "artifact-id";
  protected static final String MULE_APPLICATION = "mule-application";
  protected static final String MULE_DOMAIN = "mule-domain";
  protected static final String MULE_POLICY = "mule-policy";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public TemporaryFolder projectBaseFolder = new TemporaryFolder();

  private MuleProjectValidator validator;

  @Before
  public void before() throws IOException, MojoExecutionException {
    validator = new MuleProjectValidator(projectBaseFolder.getRoot().toPath(), MULE_APPLICATION, mock(Project.class),
                                         mock(MulePluginResolver.class), new ArrayList<>());
  }

  @Test
  public void isMuleApplicationPackagingTypeValid() throws ValidationException {
    Boolean valid = validator.isPackagingTypeValid(MULE_APPLICATION);
    assertThat("Packaging type should be valid", valid, is(true));
  }

  @Test
  public void isMuleDomainPackagingTypeValid() throws ValidationException {
    Boolean valid = validator.isPackagingTypeValid(MULE_DOMAIN);
    assertThat("Packaging type should be valid", valid, is(true));
  }

  @Test
  public void isMulePolicyPackagingTypeValid() throws ValidationException {
    Boolean valid = validator.isPackagingTypeValid(MULE_POLICY);
    assertThat("Packaging type should be valid", valid, is(true));
  }

  @Test(expected = ValidationException.class)
  public void isPackagingTypeValid() throws ValidationException {
    Boolean valid = validator.isPackagingTypeValid("no-valid-packagin");
    assertThat("Packaging type should be valid", valid, is(true));
  }
}
