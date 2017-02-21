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

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.mule.tools.maven.repository.RepositoryGenerator;

import java.util.List;

@Mojo(name = "process-sources",
    defaultPhase = LifecyclePhase.PROCESS_SOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ProcessSourcesMojo extends AbstractMuleMojo {

    @Component
    private RepositorySystem repositorySystem;

    @Component
    private ProjectBuilder projectBuilder;

    @Parameter(readonly = true, required = true, defaultValue = "${session}")
    private MavenSession session;

    @Parameter(readonly = true, required = true, defaultValue = "${project.remoteArtifactRepositories}")
    private List<ArtifactRepository> remoteArtifactRepositories;

    @Parameter(readonly = true, required = true, defaultValue = "${localRepository}")
    private ArtifactRepository localRepository;

    private ProjectBuildingRequest projectBuildingRequest;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        RepositoryGenerator repositoryGenerator = new RepositoryGenerator(session,
                                                                          project,
                                                                          projectBuilder,
                                                                          repositorySystem,
                                                                          localRepository,
                                                                          remoteArtifactRepositories,
                                                                          outputDirectory,
                                                                          getLog());
        if (!lightwayPackage) {
            repositoryGenerator.generate();
        }

    }
}
