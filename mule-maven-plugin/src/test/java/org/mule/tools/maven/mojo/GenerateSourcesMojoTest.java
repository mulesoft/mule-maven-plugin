/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.maven.mojo;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

import org.mule.tools.api.packager.sources.MuleContentGenerator;

public class GenerateSourcesMojoTest extends AbstractMuleMojoTest {

  private TestCompileMojo mojo;

  @Before
  public void before() throws IOException {
    mojo = new TestCompileMojo();
  }

  @Test
  public void getPreviousRunPlaceholder() {
    assertThat(mojo.getPreviousRunPlaceholder(), is("MULE_MAVEN_PLUGIN_TEST_COMPILE_PREVIOUS_RUN_PLACEHOLDER"));
  }
}
