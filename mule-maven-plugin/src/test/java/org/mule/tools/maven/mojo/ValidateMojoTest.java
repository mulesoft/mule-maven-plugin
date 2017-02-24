/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mule.tools.artifact.archiver.api.PackagerFiles.*;

import java.io.IOException;
import java.util.Collections;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.maven.dependency.MulePluginsCompatibilityValidator;
import org.mule.tools.maven.dependency.resolver.MulePluginResolver;

public class ValidateMojoTest extends AbstractMuleMojoTest {

  private static final ValidateMojo mojo = new ValidateMojo();

  private static final String VALIDATE_GOAL_DEBUG_MESSAGE =
      "[debug] Validating Mule application...\n[debug] Validating Mule application done\n";
  private static final String VALIDATE_OTHER_DESCRIPTORS_MESSAGE =
      "Invalid Mule project. Either " + MULE_DEPLOY_PROPERTIES + " or " + MULE_CONFIG_XML
          + " files must be present in the root of application";
  private static final String VALIDATE_MANDATORY_FOLDERS_MESSAGE =
      "Invalid Mule project. Missing src/main/mule folder. This folder is mandatory";
  private static final String VALIDATE_MULE_APP_PROPERTIES_MESSAGE =
      "Invalid Mule project. Missing " + MULE_APP_PROPERTIES + " file, it must be present in the root of application";

  @Before
  public void before() throws IOException {
    mojo.muleSourceFolder = muleSourceFolderMock;
    mojo.projectBaseFolder = projectRootFolder.getRoot();
  }

  @Test
  public void validateMandatoryFoldersFailsWhenMuleSourceFolderDoesNotExistTest()
      throws MojoFailureException, MojoExecutionException {
    expectedException.expect(MojoExecutionException.class);
    expectedException.expectMessage(VALIDATE_MANDATORY_FOLDERS_MESSAGE);

    when(muleSourceFolderMock.exists()).thenReturn(false);

    mojo.execute();
  }

  @Test
  public void validateMandatoryFoldersFailsWhenMuleAppPropertiesFileDoesNotExistTest()
      throws MojoFailureException, MojoExecutionException, IOException {
    expectedException.expect(MojoExecutionException.class);
    expectedException.expectMessage(VALIDATE_MULE_APP_PROPERTIES_MESSAGE);

    when(muleSourceFolderMock.exists()).thenReturn(true);

    mojo.execute();
  }

  @Test
  public void validateMandatoryFoldersFailsWhenMuleDeployPropertiesFileAndMuleConfigFileDoNotExistTest()
      throws MojoFailureException, MojoExecutionException, IOException {
    expectedException.expect(MojoExecutionException.class);
    expectedException.expectMessage(VALIDATE_OTHER_DESCRIPTORS_MESSAGE);

    when(muleSourceFolderMock.exists()).thenReturn(true);

    projectRootFolder.newFile(MULE_APP_PROPERTIES);

    mojo.execute();
  }

  @Test
  public void validateGoalSucceedTest() throws MojoFailureException, MojoExecutionException, IOException {
    when(muleSourceFolderMock.exists()).thenReturn(true);

    projectRootFolder.newFile(MULE_APP_PROPERTIES);
    projectRootFolder.newFile(MULE_CONFIG_XML);

    MulePluginResolver resolverMock = mock(MulePluginResolver.class);
    MulePluginsCompatibilityValidator validatorMock = mock(MulePluginsCompatibilityValidator.class);
    when(resolverMock.resolveMulePlugins(any())).thenReturn(Collections.emptyList());

    class ValidateMojoWithMockedResolverAndValidate extends ValidateMojo {

      @Override
      protected void initializeResolver() {
        this.resolver = resolverMock;
      }

      @Override
      protected void initializeValidator() {
        this.validator = validatorMock;
      }
    }

    ValidateMojo mojo = new ValidateMojoWithMockedResolverAndValidate();

    mojo.muleSourceFolder = muleSourceFolderMock;
    mojo.projectBaseFolder = projectRootFolder.getRoot();

    mojo.execute();

    verify(resolverMock, times(1)).resolveMulePlugins(any());
    verify(validatorMock, times(1)).validate(anyList());
    assertThat("Validate goal message was not the expected", VALIDATE_GOAL_DEBUG_MESSAGE, equalTo(outContent.toString()));
  }
}
