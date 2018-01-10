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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class GenerateTestResourcesMojoTest extends AbstractMuleMojoTest {

  private GenerateTestResourcesMojo mojo;

  @Before
  public void before() throws IOException {
    mojo = new GenerateTestResourcesMojo();
  }

  @Test
  public void getPreviousRunPlaceholder() {
    assertThat(mojo.getPreviousRunPlaceholder(), is("MULE_MAVEN_PLUGIN_GENERATE_TEST_RESOURCES_PREVIOUS_RUN_PLACEHOLDER"));
  }
}
