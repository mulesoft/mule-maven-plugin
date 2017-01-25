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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.ProjectBuildingException;
import org.mule.tools.maven.dependency.ApplicationDependencySelector;
import org.mule.tools.maven.dependency.resolver.MulePluginResolver;
import org.mule.tools.maven.util.CopyFileVisitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copy resource to the proper places
 */
@Mojo(name = "generate-sources",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class GenerateSourcesMojo extends AbstractMuleMojo {


    protected MulePluginResolver mulePluginResolver;
    protected ApplicationDependencySelector applicationDependencySelector;

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().debug("Creating target content with Mule source code...");

        try {
            createLibFolderContent();
            createMuleFolderContent();

            createPluginsFolderContent();

            createDescriptorFilesContent();
            createMuleSourceFolderContent();
        } catch (IOException | ProjectBuildingException e) {
            throw new MojoFailureException("Fail to generate sources", e);
        }
    }

    protected void createMuleFolderContent() throws IOException {
        File targetFolder = Paths.get(project.getBuild().getDirectory(), MULE).toFile();
        Files.walkFileTree(muleSourceFolder.toPath(), new CopyFileVisitor(muleSourceFolder, targetFolder));
    }

    protected void createLibFolderContent() throws IOException {
        addJarsFromProjectCompileDependencies();
        addJarsFromProjectLibFolder();
    }

    protected void addJarsFromProjectCompileDependencies() throws IOException {
        List<Artifact> dependencies = project.getArtifacts().stream()
            .filter(d -> d.getType().equals("jar"))
            .filter(d -> d.getScope().equals("compile"))
            .filter(d -> d.getClassifier() == null || !d.getClassifier().equals("mule-plugin"))

            .collect(Collectors.toList());

        for (File f : dependencies.stream().map(a -> a.getFile()).collect(Collectors.toList())) {
            File targetFolder = Paths.get(project.getBuild().getDirectory(), LIB).toFile();
            Path sourceFilePath = f.toPath();
            Path targetFilePath = new File(targetFolder.toPath().toString() + File.separator + f.getName()).toPath();
            Files.copy(sourceFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    protected void addJarsFromProjectLibFolder() throws IOException {
        File targetFolder = Paths.get(project.getBuild().getDirectory(), LIB).toFile();
        Files.walkFileTree(libFolder.toPath(), new CopyFileVisitor(libFolder, targetFolder));
    }

    protected void createPluginsFolderContent() throws IOException, MojoExecutionException, ProjectBuildingException {
        initializeResolver();
        initializeApplicationDependencySelector();
        List<Dependency> mulePluginDependencies = mulePluginResolver.resolveMulePlugins(project);

        List<Dependency> selectedMulePlugins = applicationDependencySelector.select(mulePluginDependencies);

        List<File> selectedMulePluginFiles = selectedMulePlugins.stream()
            .map(smp -> localRepository.find(repositorySystem.createDependencyArtifact(smp)).getFile())
            .collect(Collectors.toList());

        for (File f : selectedMulePluginFiles) {
            File targetFolder = Paths.get(project.getBuild().getDirectory(), PLUGINS).toFile();
            Path sourceFilePath = f.toPath();
            Path targetFilePath = new File(targetFolder.toPath().toString() + File.separator + f.getName()).toPath();
            Files.copy(sourceFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
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

    protected void createDescriptorFilesContent() throws IOException {
        copyFileFromRootToRoot(POM_XML);
        copyFileFromRootToRoot(MULE_APP_PROPERTIES);
        copyFileFromRootToRoot(MULE_DEPLOY_PROPERTIES);
    }

    protected void copyFileFromRootToRoot(String source) throws IOException {
        File targetFolder = new File(project.getBuild().getDirectory());
        Path sourceFilePath = new File(projectBaseFolder.getCanonicalPath() + File.separator + source).toPath();
        Path targetFilePath = new File(targetFolder.toPath().toString() + File.separator + source).toPath();
        Files.copy(sourceFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    protected void initializeResolver() {
        mulePluginResolver = new MulePluginResolver(getLog(), session, projectBuilder, repositorySystem, localRepository,
                remoteArtifactRepositories);
    }

    protected void initializeApplicationDependencySelector() {
        applicationDependencySelector = new ApplicationDependencySelector();
    }


}
