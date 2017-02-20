/**
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import org.apache.maven.Maven;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.mule.tools.maven.util.CopyFileVisitor;
import org.mule.tools.maven.util.DescriptorManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Copy resource to the proper places
 */
@Mojo(name = "generate-sources",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GenerateSourcesMojo extends AbstractMuleMojo {
    DescriptorManager descriptorManager;
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Creating target content with Mule source code...");
        descriptorManager = new DescriptorManager(project, projectBaseFolder);
        try {
            createMuleFolderContent();
            createMuleSourceFolderContent();
            createDescriptors();
        } catch (IOException e) {
            throw new MojoFailureException("Fail to generate sources", e);
        }
    }

    protected void createMuleFolderContent() throws IOException {
        File targetFolder = Paths.get(project.getBuild().getDirectory(), MULE).toFile();
        Files.walkFileTree(muleSourceFolder.toPath(), new CopyFileVisitor(muleSourceFolder, targetFolder));
    }

    protected void createMuleSourceFolderContent() throws IOException {
        //TODO create ignore concept for things like .settings

        File targetFolder = Paths.get(project.getBuild().getDirectory(), META_INF, MULE_SRC, project.getArtifactId()).toFile();
        CopyFileVisitor visitor = new CopyFileVisitor(projectBaseFolder, targetFolder);
        List<Path> exclusions = new ArrayList<>();
        exclusions.add(Paths.get(projectBaseFolder.toPath().toString(), TARGET));
        visitor.setExclusions(exclusions);
        Files.walkFileTree(projectBaseFolder.toPath(), visitor);
    }

    private void createDescriptors() throws IOException, MojoExecutionException {
        createDescriptorFilesContent();
        createApplicationDependencyDescriptor();
        createPomProperties();
    }

    protected void createDescriptorFilesContent() throws IOException {
        descriptorManager.copyDescriptor(POM_XML).toPath(META_INF, MAVEN, project.getGroupId(), project.getArtifactId());
        descriptorManager.copyDescriptor(MULE_APP_PROPERTIES).toPath(META_INF, MULE_ARTIFACT);
        descriptorManager.copyDescriptor(MULE_DEPLOY_PROPERTIES).toPath(META_INF, MULE_ARTIFACT);
    }

    protected void createApplicationDependencyDescriptor() throws IOException {
        descriptorManager.copyDescriptor(MULE_APP_JSON).toPath(META_INF, MULE_ARTIFACT);
        Path sourceFilePath = new File(projectBaseFolder.getCanonicalPath() + File.separator + MULE_APP_JSON).toPath();
        Files.delete(sourceFilePath);
    }

    protected void createPomProperties() throws IOException, MojoExecutionException {
        File targetFolder = Paths.get(project.getBuild().getDirectory(), META_INF, MAVEN, project.getGroupId(), project.getArtifactId()).toFile();
        Path targetFilePath = new File(targetFolder.toPath().toString() + File.separator + POM_PROPERTIES).toPath();
        writeToFile(targetFilePath);
    }

    private void writeToFile(Path targetFilePath) throws MojoExecutionException {
        try{
            PrintWriter writer = new PrintWriter(targetFilePath.toString(), "UTF-8");
            writer.println("version=" + this.project.getVersion());
            writer.println("groupId=" + this.project.getGroupId());
            writer.println("artifactId=" + this.project.getArtifactId());
            writer.close();
        } catch (IOException e) {
            throw new MojoExecutionException("Could not create pom.properties", e);
        }
    }
}
