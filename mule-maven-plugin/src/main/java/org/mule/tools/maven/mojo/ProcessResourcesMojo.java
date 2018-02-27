/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.mule.tools.api.packager.resources.MuleResourcesGenerator;
import org.mule.tools.api.util.Artifact;
import org.mule.tools.maven.utils.Exclusion;
import org.mule.tools.maven.utils.Inclusion;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mojo that runs on the {@link LifecyclePhase#PROCESS_RESOURCES}
 */
@Mojo(name = "process-resources",
    defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ProcessResourcesMojo extends AbstractMuleMojo {

  /**
   * List of exclusion elements (having groupId and artifactId children) to exclude from the application archive.
   *
   * @parameter
   * @since 2.3.0
   */
  @Parameter
  private List<Exclusion> exclusions;

  /**
   * List of inclusion elements (having groupId and artifactId children) to exclude from the application archive.
   *
   * @parameter
   * @since 2.3.0
   */
  @Parameter
  private List<Inclusion> inclusions;

  /**
   * Exclude all artifacts with Mule groupIds. Default is <code>true</code>.
   *
   * @parameter default-value="true"
   * @since 2.3.0
   */
  @Parameter(defaultValue = "true")
  private boolean excludeMuleDependencies;

  /**
   * @parameter default-value="false"
   * @since 2.3.0
   */
  @Parameter(defaultValue = "false", property = "prependGroupId")
  private boolean prependGroupId;


  @Override
  public void doExecute() throws MojoExecutionException, MojoFailureException {
    try {
      getResourcesContentGenerator().generate(prependGroupId);
    } catch (IllegalArgumentException | IOException e) {
      throw new MojoFailureException("Fail to generate resources", e);
    }
  }

  public MuleResourcesGenerator getResourcesContentGenerator() {
    Set<Artifact> projectArtifacts = new HashSet<>();
    for (org.apache.maven.artifact.Artifact artifact : project.getArtifacts()) {
      projectArtifacts.add(new org.mule.tools.maven.utils.Artifact(artifact));
    }

    return new MuleResourcesGenerator(projectArtifacts, exclusions, inclusions, excludeMuleDependencies,
                                      getAndSetProjectInformation());
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_GENERATE_RESOURCES_PREVIOUS_RUN_PLACEHOLDER";
  }
}
