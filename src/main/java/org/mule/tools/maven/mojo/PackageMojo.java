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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.ArchiverException;
import org.mule.tools.artifact.archiver.api.PackageBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Build a Mule application archive.
 */
@Mojo(name = "package",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class PackageMojo extends AbstractMuleMojo {

    @Component
    private MavenProjectHelper projectHelper;

    @Parameter(defaultValue = "${attachMuleSources}")
    protected boolean attachMuleSources = false;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            createMuleApp();
        } catch (ArchiverException e) {
            throw new MojoExecutionException("Exception creating the Mule App", e);
        }
    }

    protected void createMuleApp() throws MojoExecutionException, ArchiverException {
        String targetFolder = project.getBuild().getDirectory();
        File destinationFile = new File(targetFolder, project.getArtifactId() + ".zip");

        try {
            PackageBuilder builder = new PackageBuilder()
                .withDestinationFile(destinationFile)
                .withClasses(new File(targetFolder + File.separator + CLASSES))
                .withLib(new File(targetFolder + File.separator + LIB))
                .withMule(new File(targetFolder + File.separator + MULE))
                .withPlugins(new File(targetFolder + File.separator + PLUGINS))
                .withMuleAppProperties(new File(targetFolder + File.separator + MULE_APP_PROPERTIES))
                .withMuleDeployProperties(new File(targetFolder + File.separator + MULE_DEPLOY_PROPERTIES))
                .withPom(new File(targetFolder + File.separator + POM_XML));

            if (attachMuleSources) {
                builder.withMetaInf(new File(targetFolder + File.separator + META_INF));
            }

            builder.createDeployableFile();
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create archive");
        }
    }

}
