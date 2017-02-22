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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.mule.tools.maven.util.ProjectBaseFolderFileCloner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GenerateSourcesMojoTest extends AbstractMuleMojoTest {

    private GenerateSourcesMojo mojo = new GenerateSourcesMojo();

    @Before
    public void before() throws IOException {
        mojo = new GenerateSourcesMojo();
        mojo.projectBaseFolder = projectRootFolder.getRoot();
        mojo.project = projectMock;
    }

    @Test
    public void createMuleFolderContentTest() throws IOException, MojoFailureException, MojoExecutionException {
        muleSourceFolderMock =
            new File(projectRootFolder.getRoot().getAbsolutePath(), "src" + File.separator + "main" + File.separator + "mule");
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
        assertThat("The mule folder content is different from the expected",
                   StringUtils.equals(actualCopiedFile1.getName(), fileToBeCopied1.getName()) && StringUtils
                       .equals(actualCopiedFile2.getName(), fileToBeCopied2.getName()));
    }

    @Test
    public void createMuleSourceFolderContentTest() throws IOException {
        File muleSourceFolder = new File(metaInfFolder.getAbsolutePath(), MULE_SRC);
        File projectArtifactIdFolder = new File(muleSourceFolder.getAbsolutePath(), PROJECT_ARTIFACT_ID);

        metaInfFolder.mkdir();
        muleSourceFolder.mkdir();
        projectArtifactIdFolder.mkdir();

        File fileToBeCopied1 = new File(projectRootFolder.getRoot().getAbsolutePath(), "file1");
        fileToBeCopied1.createNewFile();
        File fileToBeCopied2 = new File(projectRootFolder.getRoot().getAbsolutePath(), "file2");
        fileToBeCopied2.createNewFile();

        when(projectMock.getArtifactId()).thenReturn(PROJECT_ARTIFACT_ID);
        mojo.createMuleSourceFolderContent();

        assertThat("There should be 2 files in the target folder", projectArtifactIdFolder.listFiles().length, equalTo(2));

        File actualCopiedFile1 = projectArtifactIdFolder.listFiles()[0];
        assertThat("The mule folder content is different from the expected",
                   actualCopiedFile1.getName().equals(fileToBeCopied1.getName()));


        File actualCopiedFile2 = projectArtifactIdFolder.listFiles()[1];
        assertThat("The mule folder content is different from the expected",
                   actualCopiedFile2.getName().equals(fileToBeCopied2.getName()));
    }

    @Test
    public void createDescriptorFilesContentTest() throws IOException {
        File pomDestinationFolder = new File(projectRootFolder.getRoot().getAbsolutePath(),
                                             META_INF + File.separator + MAVEN + File.separator + GROUP_ID + File.separator
                                                 + ARTIFACT_ID);
        pomDestinationFolder.mkdirs();

        File propertiesDestinationFolder =
            new File(projectRootFolder.getRoot().getAbsolutePath(), META_INF + File.separator + MULE_ARTIFACT);
        propertiesDestinationFolder.mkdirs();

        File pom = projectRootFolder.newFile(POM_XML);
        pom.createNewFile();
        File muleAppPropertiesFile = projectRootFolder.newFile(MULE_APP_PROPERTIES);
        muleAppPropertiesFile.createNewFile();
        File muleDeployPropertiesFile = projectRootFolder.newFile(MULE_DEPLOY_PROPERTIES);
        muleDeployPropertiesFile.createNewFile();

        buildMock = mock(Build.class);
        projectMock = mock(MavenProject.class);
        when(buildMock.getDirectory()).thenReturn(projectRootFolder.getRoot().getAbsolutePath());
        when(projectMock.getBuild()).thenReturn(buildMock);
        when(projectMock.getGroupId()).thenReturn(GROUP_ID);
        when(projectMock.getArtifactId()).thenReturn(ARTIFACT_ID);
        when(projectMock.getBasedir()).thenReturn(projectRootFolder.getRoot());

        mojo.project = projectMock;
        mojo.projectBaseFolderFileCloner = new ProjectBaseFolderFileCloner(projectMock);

        mojo.createDescriptorFilesContent();

        assertThat("There should be 1 file in the pom destination folder", pomDestinationFolder.listFiles().length, equalTo(1));
        List<File> actualFilesInPomDestinationFolder =
            Arrays.asList(pomDestinationFolder.listFiles()).stream().filter(file -> file.isFile()).collect(Collectors.toList());
        assertThat("The pom destination folder does not contains the expected file",
                   actualFilesInPomDestinationFolder.get(0).getName(), equalTo(POM_XML));

        assertThat("There should be 2 files in the properties destination folder", propertiesDestinationFolder.listFiles().length,
                   equalTo(2));
        List<File> actualFilesInPropertiesDestinationFolder =
            Arrays.asList(propertiesDestinationFolder.listFiles()).stream().filter(file -> file.isFile())
                .collect(Collectors.toList());
        assertThat("The properties destination folder does not contains the expected files",
                   actualFilesInPropertiesDestinationFolder.stream().map(file -> file.getName()).collect(Collectors.toList()),
                   containsInAnyOrder(MULE_APP_PROPERTIES, MULE_DEPLOY_PROPERTIES));
    }
}
