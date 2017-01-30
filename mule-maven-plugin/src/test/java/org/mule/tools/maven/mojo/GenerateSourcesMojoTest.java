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

import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.artifact.Artifact;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.repository.RepositorySystem;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.junit.Test;
import org.junit.Before;
import org.mule.tools.maven.dependency.ApplicationDependencySelector;
import org.mule.tools.maven.dependency.resolver.MulePluginResolver;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class GenerateSourcesMojoTest extends AbstractMuleMojoTest {
    GenerateSourcesMojo mojo = new GenerateSourcesMojo();
    @Before
    public void before() throws IOException {
        mojo = new GenerateSourcesMojo();
        mojo = new GenerateSourcesMojo();
        projectMock = mock(MavenProject.class);
        buildMock = mock(Build.class);
        mojo.projectBaseFolder = temporaryFolder.getRoot();
        System.setOut(new PrintStream(outContent));
        when(buildMock.getDirectory()).thenReturn(buildTemporaryFolder.getRoot().getAbsolutePath());
        when(projectMock.getBuild()).thenReturn(buildMock);
        mojo.project = projectMock;
    }

    @Test
    public void createMuleFolderContentTest() throws IOException, MojoFailureException, MojoExecutionException {
        muleSourceFolderMock = new File(temporaryFolder.getRoot().getAbsolutePath(), "src" + File.separator + "main" + File.separator + "mule");
        muleSourceFolderMock.mkdirs();
        File fileToBeCopied1 = new File(muleSourceFolderMock.getAbsolutePath(), "file1");
        File fileToBeCopied2 = new File(muleSourceFolderMock.getAbsolutePath(), "file2");
        fileToBeCopied1.createNewFile();
        fileToBeCopied2.createNewFile();
        mojo.muleSourceFolder = muleSourceFolderMock;

        File muleFolder = buildTemporaryFolder.newFolder(MULE);
        muleFolder.mkdir();
        mojo.createMuleFolderContent();

        assertThat("The mule folder does not contain the expected number of files", muleFolder.listFiles().length, equalTo(2));
        File actualCopiedFile1 = muleFolder.listFiles()[0];
        File actualCopiedFile2 = muleFolder.listFiles()[1];
        assertThat("The mule folder content is different from the expected", StringUtils.equals(actualCopiedFile1.getName(), fileToBeCopied1.getName()) && StringUtils.equals(actualCopiedFile2.getName(), fileToBeCopied2.getName()));
    }

    @Test
    public void addJarsFromProjectCompileDependenciesTest() throws IOException {
        muleSourceFolderMock = new File(temporaryFolder.getRoot().getAbsolutePath(), "src" + File.separator + "main" + File.separator + "mule");
        muleSourceFolderMock.mkdirs();
        Artifact artifactMock1 = mock(Artifact.class);

        when(artifactMock1.getType()).thenReturn("jar");
        when(artifactMock1.getScope()).thenReturn("compile");
        when(artifactMock1.getClassifier()).thenReturn(null);

        File artifactFile1 = new File(muleSourceFolderMock.getAbsolutePath(), "file1");
        artifactFile1.createNewFile();

        when(artifactMock1.getFile()).thenReturn(artifactFile1);

        Artifact artifactMock2 = mock(Artifact.class);

        when(artifactMock2.getType()).thenReturn("jar");
        when(artifactMock2.getScope()).thenReturn("compile");
        when(artifactMock2.getClassifier()).thenReturn(null);

        File artifactFile2 = new File(muleSourceFolderMock.getAbsolutePath(), "file2");
        artifactFile2.createNewFile();

        when(artifactMock2.getFile()).thenReturn(artifactFile2);

        Set<Artifact> artifacts = new HashSet<>();
        artifacts.add(artifactMock1);
        artifacts.add(artifactMock2);

        when(projectMock.getArtifacts()).thenReturn(artifacts);

        File libFolder = buildTemporaryFolder.newFolder(LIB);
        libFolder.mkdir();
        mojo.addJarsFromProjectCompileDependencies();

        assertThat("The lib folder should contain two file", libFolder.listFiles().length, equalTo(2));
        assertThat("The lib folder should contain artifact1", libFolder.listFiles()[0].getName(), equalTo(artifactFile1.getName()));
        assertThat("The lib folder should contain artifact2", libFolder.listFiles()[1].getName(), equalTo(artifactFile2.getName()));
    }

    @Test
    public void addJarsFromProjectLibFolderTest() throws IOException, MojoFailureException, MojoExecutionException {
        File projectLibFolder = new File(temporaryFolder.getRoot().getAbsolutePath(), LIB);
        projectLibFolder.mkdir();
        File fileToBeCopied1 = new File(projectLibFolder.getAbsolutePath(), "file1");
        File fileToBeCopied2 = new File(projectLibFolder.getAbsolutePath(), "file2");
        fileToBeCopied1.createNewFile();
        fileToBeCopied2.createNewFile();
        mojo.libFolder = projectLibFolder;

        File libFolder = buildTemporaryFolder.newFolder(LIB);
        libFolder.mkdir();

        mojo.addJarsFromProjectLibFolder();

        assertThat("The mule folder does not contain the expected number of files", libFolder.listFiles().length, equalTo(2));
        File actualCopiedFile1 = libFolder.listFiles()[0];
        File actualCopiedFile2 = libFolder.listFiles()[1];
        assertThat("The mule folder content is different from the expected", StringUtils.equals(actualCopiedFile1.getName(), fileToBeCopied1.getName()) && StringUtils.equals(actualCopiedFile2.getName(), fileToBeCopied2.getName()));
    }

    @Test
    public void createMuleSourceFolderContentTest() throws IOException {
        File metaInfFolder = buildTemporaryFolder.newFolder(META_INF);
        File muleSource = new File(metaInfFolder.getAbsolutePath(), MULE_SRC);
        File targetFolder = new File(muleSource.getAbsolutePath(), PROJECT_ARTIFACT_ID);

        metaInfFolder.mkdir();
        muleSource.mkdir();
        targetFolder.mkdir();

        when(projectMock.getArtifactId()).thenReturn(PROJECT_ARTIFACT_ID);

        File fileToBeCopied1 = new File(temporaryFolder.getRoot().getAbsolutePath(), "file1");
        File fileToBeCopied2 = new File(temporaryFolder.getRoot().getAbsolutePath(), "file2");
        fileToBeCopied1.createNewFile();
        fileToBeCopied2.createNewFile();

        mojo.createMuleSourceFolderContent();

        assertThat("There should be 2 files in the target folder", targetFolder.listFiles().length, equalTo(2));
        File actualCopiedFile1 = targetFolder.listFiles()[0];
        File actualCopiedFile2 = targetFolder.listFiles()[1];
        assertThat("The mule folder content is different from the expected", StringUtils.equals(actualCopiedFile1.getName(), fileToBeCopied1.getName()) && StringUtils.equals(actualCopiedFile2.getName(), fileToBeCopied2.getName()));
    }

    @Test
    public void createDescriptorFilesContentTest() throws IOException {
        File pom = temporaryFolder.newFile(POM_XML);
        File muleAppPropertiesFile = temporaryFolder.newFile(MULE_APP_PROPERTIES);
        File muleDeployPropertiesFile = temporaryFolder.newFile(MULE_DEPLOY_PROPERTIES);

        pom.createNewFile();
        muleAppPropertiesFile.createNewFile();
        muleDeployPropertiesFile.createNewFile();

        mojo.createDescriptorFilesContent();

        assertThat("There should be 3 files in the project base folder", buildTemporaryFolder.getRoot().listFiles().length, equalTo(3));
        List<File> actualFiles = Arrays.asList(buildTemporaryFolder.getRoot().listFiles()).stream().filter(file -> file.isFile()).collect(Collectors.toList());
        assertThat("The target folder does not contains the expected files", actualFiles.stream().map(file -> file.getName()).collect(Collectors.toList()), containsInAnyOrder(POM_XML, MULE_APP_PROPERTIES, MULE_DEPLOY_PROPERTIES));
    }

    @Test
    public void createPluginsFolderContentTest() throws MojoExecutionException, IOException, ProjectBuildingException {
        File pluginsFolder = buildTemporaryFolder.newFolder(PLUGINS);
        pluginsFolder.mkdir();

        MulePluginResolver mulePluginResolverMock = mock(MulePluginResolver.class);
        ApplicationDependencySelector applicationDependencySelectorMock = mock(ApplicationDependencySelector.class);

        List<Dependency> mulePluginDependenciesMock = new ArrayList<>();
        List<Dependency> selectedMulePluginsMock = new ArrayList<>();

        Dependency dependencyMock = mock(Dependency.class);
        selectedMulePluginsMock.add(dependencyMock);
        mulePluginDependenciesMock.add(dependencyMock);

        when(applicationDependencySelectorMock.select(mulePluginDependenciesMock)).thenReturn(selectedMulePluginsMock);
        when(mulePluginResolverMock.resolveMulePlugins(projectMock)).thenReturn(mulePluginDependenciesMock);

        class GenerateSourcesMojoImpl extends GenerateSourcesMojo {
            @Override
            protected void initializeResolver() {
                mulePluginResolver = mulePluginResolverMock;
            }
            @Override
            protected void initializeApplicationDependencySelector() {
                applicationDependencySelector = applicationDependencySelectorMock;
            }
        }

        ArtifactRepository localRepositoryMock = mock(ArtifactRepository.class);
        RepositorySystem repositorySystemMock = mock(RepositorySystem.class);
        Artifact artifactMock = mock(Artifact.class);

        File artifactFile = temporaryFolder.newFile(ARTIFACT_FILE_NAME);
        when(artifactMock.getFile()).thenReturn(artifactFile);
        when(localRepositoryMock.find(artifactMock)).thenReturn(artifactMock);
        when(repositorySystemMock.createDependencyArtifact(dependencyMock)).thenReturn(artifactMock);

        mojo = new GenerateSourcesMojoImpl();

        mojo.project = projectMock;
        mojo.localRepository = localRepositoryMock;
        mojo.repositorySystem = repositorySystemMock;

        mojo.createPluginsFolderContent();

        assertThat("There should be 1 file in the plugins folder", pluginsFolder.listFiles().length, equalTo(1));
        List<File> actualFiles = Arrays.asList(pluginsFolder.listFiles()).stream().filter(file -> file.isFile()).collect(Collectors.toList());
        assertThat("The plugins folder does not contains the expected file", actualFiles.get(0).getName(), equalTo(ARTIFACT_FILE_NAME));
    }
}