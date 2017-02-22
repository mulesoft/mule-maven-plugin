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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.artifact.AttachedArtifact;
import org.codehaus.plexus.archiver.ArchiverException;
import org.mule.tools.artifact.archiver.api.PackageBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Build a Mule application archive.
 */
@Mojo(name = "package",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class PackageMojo extends AbstractMuleMojo {

    private static final String TYPE = "zip";

    @Component
    protected ArtifactHandlerManager handlerManager;

    @Parameter(defaultValue = "${finalName}")
    protected String finalName;

    @Parameter(defaultValue = "${onlyMuleSources}")
    protected boolean onlyMuleSources = false;

    @Parameter(defaultValue = "${attachMuleSources}")
    protected boolean attachMuleSources = false;

    protected PackageBuilder packageBuilder;

    public void execute() throws MojoExecutionException, MojoFailureException {
        String targetFolder = project.getBuild().getDirectory();
        File destinationFile = getDestinationFile(targetFolder);
        try {
            createMuleApp(destinationFile, targetFolder);
        } catch (ArchiverException e) {
            throw new MojoExecutionException("Exception creating the Mule App", e);
        }
        setProjectArtifactTypeToZip(destinationFile);
    }

    /**
     * Given a {@code targetFolder}, it returns a new {@link File} to the new compressed file where the complete Mule app will be
     * stored. If the file already exists, it will delete it and create a new one.
     *
     * @param targetFolder starting path in which the destination file will be stored
     * @return the destination file to store the Mule app
     * @throws MojoExecutionException if it can't delete the previous file
     */
    private File getDestinationFile(String targetFolder) throws MojoExecutionException {
        final Path destinationPath = Paths.get(targetFolder, getFinalName() + "." + TYPE);
        try {
            Files.deleteIfExists(destinationPath);
        } catch (IOException e) {
            throw new MojoExecutionException(String.format("Exception deleting the file [%s]", destinationPath), e);
        }
        return destinationPath.toFile();
    }

    private String getFinalName() {
        if (finalName == null) {
            finalName = project.getArtifactId() + "-" + project.getVersion();
        }
        getLog().debug("Using final name: " + finalName);
        return finalName;
    }

    protected void setProjectArtifactTypeToZip(File destinationFile) {
        ArtifactHandler handler = handlerManager.getArtifactHandler(TYPE);
        Artifact artifact = new AttachedArtifact(this.project.getArtifact(), TYPE, handler);
        artifact.setFile(destinationFile);
        artifact.setResolved(true);
        this.project.setArtifact(artifact);
    }

    protected void createMuleApp(File destinationFile, String targetFolder) throws MojoExecutionException, ArchiverException {
        initializePackageBuilder();
        try {
            PackageBuilder builder = packageBuilder.withDestinationFile(destinationFile);
            if (!onlyMuleSources) {
                builder
                    .withClasses(new File(targetFolder + File.separator + CLASSES))
                    .withMule(new File(targetFolder + File.separator + MULE));

                if (!lightwayPackage) {
                    builder.withRepository(new File(targetFolder + File.separator + REPOSITORY));
                }
                builder.withMetaInf(new File(targetFolder + File.separator + META_INF));
                // TODO  review this api so we can add mule sources at will
                //                if (attachMuleSources) {
                //                    builder.withMetaInf(new File(targetFolder + File.separator + META_INF));
                //                }

            } else {
                builder.withMetaInf(new File(targetFolder + File.separator + META_INF));
            }

            builder.createDeployableFile();
        } catch (IOException e) {
            throw new MojoExecutionException("Cannot create archive");
        }
    }

    protected void initializePackageBuilder() {
        packageBuilder = new PackageBuilder();
    }
}
