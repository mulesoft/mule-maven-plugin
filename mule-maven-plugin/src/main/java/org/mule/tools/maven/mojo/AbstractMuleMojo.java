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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.repository.RepositorySystem;

import java.io.File;
import java.util.List;

/**
 * Base Mojo
 */
public abstract class AbstractMuleMojo extends AbstractMojo {

    public static final String POM_XML = "pom.xml";
    public static final String MULE_CONFIG_XML = "mule-config.xml";
    public static final String MULE_APP_PROPERTIES = "mule-app.properties";
    public static final String MULE_DEPLOY_PROPERTIES = "mule-deploy.properties";

    public static final String LIB = "lib";
    public static final String MULE = "mule";
    public static final String TEST_MULE = "test-mule";
    public static final String MUNIT = "munit";
    public static final String TARGET = "target";
    public static final String PLUGINS = "plugins";
    public static final String CLASSES = "classes";
    public static final String MULE_SRC = "mule-src";
    public static final String META_INF = "META-INF";

    @Component
    protected RepositorySystem repositorySystem;

    @Component
    protected ProjectBuilder projectBuilder;

    @Parameter(readonly = true, required = true, defaultValue = "${session}")
    protected MavenSession session;

    @Parameter(readonly = true, required = true, defaultValue = "${project.remoteArtifactRepositories}")
    protected List<ArtifactRepository> remoteArtifactRepositories;

    @Parameter(readonly = true, required = true, defaultValue = "${localRepository}")
    protected ArtifactRepository localRepository;

    @Parameter(property = "project", required = true)
    protected MavenProject project;

    // TODO remove this as is part of the maven project and it should be on the target
    @Parameter(property = "project.build.directory", required = true)
    protected File outputDirectory;

    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    protected String finalName;

    @Parameter(defaultValue = "${project.basedir}")
    protected File projectBaseFolder;

    @Parameter(defaultValue = "${project.basedir}/plugins")
    protected File pluginsFolder;

    @Parameter(defaultValue = "${project.basedir}/src/main/mule/")
    protected File muleSourceFolder;

    @Parameter(defaultValue = "${project.basedir}/src/test/munit/")
    protected File munitSourceFolder;

    // TODO remove this
    @Parameter(defaultValue = "${project.basedir}/lib", required = false)
    protected File libFolder;

    // TODO change this to mule directory
    @Parameter(defaultValue = "${project.basedir}/src/main/app", required = true)
    protected File appDirectory;


    protected File getMuleAppZipFile() {
        return new File(this.outputDirectory, this.finalName + ".zip");
    }

    // TODO omg why
    protected File getFilteredAppDirectory() {
        return new File(outputDirectory, "app");
    }
}
