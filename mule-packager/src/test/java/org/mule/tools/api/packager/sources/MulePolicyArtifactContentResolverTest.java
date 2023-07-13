/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.sources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.maven.pom.parser.api.model.BundleDependency;
import org.mule.tools.api.packager.Pom;
import org.mule.tools.api.packager.structure.ProjectStructure;

import java.nio.file.Path;
import java.util.List;

public class MulePolicyArtifactContentResolverTest extends MuleArtifactContentResolverTest {

  private static final String TEMPLATE_XML = "template.xml";
  Path path;

  @BeforeEach
  public void setUpTemplatePath() {
    path = mock(Path.class);
    when(path.getFileName()).thenReturn(path);
    when(path.toString()).thenReturn(TEMPLATE_XML);
  }

  @Test
  public void hasMuleAsRootElementWithNullDocument() {
    assertThat(resolver.hasMuleAsRootElement(path)).describedAs("Method should have returned true");
  }

  @Override
  protected MuleArtifactContentResolver newResolver(ProjectStructure projectStructure, Pom pomMock,
                                                    List<BundleDependency> objects) {
    return new MulePolicyArtifactContentResolver(projectStructure, pomMock, objects);
  }
}
