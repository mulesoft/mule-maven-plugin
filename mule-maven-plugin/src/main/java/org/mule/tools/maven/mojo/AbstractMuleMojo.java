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
import org.apache.maven.shared.utils.StringUtils;
import org.mule.tools.maven.mojo.model.SharedLibraryDependency;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Base Mojo
 */
public abstract class AbstractMuleMojo extends AbstractMojo {

    public static final String POM_XML = "pom.xml";
    public static final String MULE_CONFIG_XML = "mule-config.xml";
    public static final String MULE_APP_PROPERTIES = "mule-app.properties";
    public static final String MULE_DEPLOY_PROPERTIES = "mule-deploy.properties";

    public static final String MULE = "mule";
    public static final String TEST_MULE = "test-mule";
    public static final String MUNIT = "munit";
    public static final String TARGET = "target";
    public static final String CLASSES = "classes";
    public static final String MULE_SRC = "mule-src";
    public static final String META_INF = "META-INF";
    public static final String MAVEN = "maven";
    public static final String MULE_APPLICATION_JSON = "mule-application.json";
    public static final String MULE_ARTIFACT = "mule-artifact";
    public static final String REPOSITORY = "repository";
    public static final String POM_PROPERTIES = "pom.properties";

    @Component
    protected ProjectBuilder projectBuilder;

    @Component
    protected RepositorySystem repositorySystem;

    @Parameter(readonly = true, required = true, defaultValue = "${session}")
    protected MavenSession session;

    @Parameter(readonly = true, required = true, defaultValue = "${localRepository}")
    protected ArtifactRepository localRepository;

    @Parameter(readonly = true, required = true, defaultValue = "${project.remoteArtifactRepositories}")
    protected List<ArtifactRepository> remoteArtifactRepositories;

    @Parameter(defaultValue = "${project.build.finalName}", required = true)
    protected String finalName;

    @Parameter(property = "project", required = true)
    protected MavenProject project;

    @Parameter(property = "project.build.directory", required = true)
    protected File outputDirectory;

    @Parameter(defaultValue = "${project.basedir}")
    protected File projectBaseFolder;

    @Parameter(defaultValue = "${project.basedir}/src/main/mule/")
    protected File muleSourceFolder;

    @Parameter(defaultValue = "${project.basedir}/src/test/munit/")
    protected File munitSourceFolder;

    @Parameter(defaultValue = "${lightwayPackage}")
    protected boolean lightwayPackage = false;

    @Parameter(property = "shared.libraries", required = false)
    protected List<SharedLibraryDependency> sharedLibraries;


    protected File getMuleAppZipFile() {
        return new File(this.outputDirectory, this.finalName + ".zip");
    }


    protected void createFileIfNecessary(String... filePath) throws IOException {
        String path = StringUtils.join(filePath, File.separator);
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    protected void createFolderIfNecessary(String... folderPath) {
        String path = StringUtils.join(folderPath, File.separator);
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }
}
