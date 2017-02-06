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

import static java.io.File.separatorChar;
import static java.lang.String.format;
import static java.util.Collections.sort;
import static org.apache.commons.io.FileUtils.copyFile;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelProblem.Severity;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.apache.maven.repository.RepositorySystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Mojo(name = "repository-mirror", defaultPhase = LifecyclePhase.VERIFY, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class RepositoryMirrorMojo extends AbstractMuleMojo {

    public static final String REPOSITORY_FOLDER = "repository";

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
        getLog().info(format("Mirroring repository for [%s]", this.project.toString()));
        try {
            initializeProjectBuildingRequest();
            final File repositoryFile = new File(outputDirectory, REPOSITORY_FOLDER);
            if (!repositoryFile.exists()) {
                repositoryFile.mkdirs();
            }
            final Set<Artifact> artifacts = new HashSet<>(project.getArtifacts());
            for (Artifact dep : new ArrayList<>(artifacts)) {
                addThirdPartyParentPomArtifacts(artifacts, dep);
            }
            addParentPomArtifacts(artifacts);

            installArtifacts(repositoryFile, artifacts);
        } catch (Exception e) {
            if (getLog().isDebugEnabled()) {
                getLog().debug(format("There was an exception while building [%s]", project.toString()), e);
            }
            throw e;
        }
    }

    private void addThirdPartyParentPomArtifacts(Set<Artifact> artifacts, Artifact dep) throws MojoExecutionException {
        final MavenProject project = buildProjectFromArtifact(dep);
        addParentDependencyPomArtifacts(project, artifacts);
        final Artifact pomArtifact =
                repositorySystem.createProjectArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
        artifacts.add(getResolvedArtifactUsingLocalRepository(pomArtifact));
    }

    private void installArtifacts(File repositoryFile, Set<Artifact> artifacts) throws MojoExecutionException {
        final List<Artifact> sortedArtifacts = new ArrayList<>(artifacts);
        sort(sortedArtifacts);
        if (sortedArtifacts.isEmpty()) {
            final File markerFile = new File(repositoryFile, ".marker");
            getLog().info(format("No artifacts to add, adding marker file <%s/%s>", REPOSITORY_FOLDER, markerFile.getName()));
            try {
                markerFile.createNewFile();
            } catch (IOException e) {
                throw new MojoExecutionException(format("The current repository has no artifacts to install, and trying to create [%s] failed",
                        markerFile.toString()),
                        e);
            }
        }
        for (Artifact artifact : sortedArtifacts) {
            installArtifact(repositoryFile, artifact);
        }
    }

    private void initializeProjectBuildingRequest() {
        projectBuildingRequest =
                new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        projectBuildingRequest.setLocalRepository(localRepository);
        projectBuildingRequest.setRemoteRepositories(remoteArtifactRepositories);
        if (getLog().isDebugEnabled()) {
            getLog().debug(format("Local repository [%s]", projectBuildingRequest.getLocalRepository().getBasedir()));
            projectBuildingRequest.getRemoteRepositories().stream().forEach(artifactRepository -> getLog()
                    .debug(format("Remote repository ID [%s], URL [%s]", artifactRepository.getId(), artifactRepository.getUrl())));
        }
    }

    private void installArtifact(File repositoryFile, Artifact artifact) throws MojoExecutionException {
        final File artifactFolderDestination = getFormattedOutputDirectory(repositoryFile, artifact);
        final String artifactFilename = getFormattedFileName(artifact);

        if (!artifactFolderDestination.exists()) {
            artifactFolderDestination.mkdirs();
        }
        final File destinationArtifactFile = new File(artifactFolderDestination, artifactFilename);
        try {
            getLog().info(format("Adding artifact <%s%s>",
                    REPOSITORY_FOLDER,
                    destinationArtifactFile.getAbsolutePath().replaceFirst(Pattern.quote(repositoryFile.getAbsolutePath()),
                            "")));
            copyFile(artifact.getFile(), destinationArtifactFile);
        } catch (IOException e) {
            throw new MojoExecutionException(format("There was a problem while copying the artifact [%s] file [%s] to the destination [%s]",
                    artifact.toString(), artifact.getFile().getAbsolutePath(),
                    destinationArtifactFile.getAbsolutePath()),
                    e);
        }
    }

    private MavenProject buildProjectFromArtifact(Artifact artifact)
            throws MojoExecutionException {
        MavenProject mavenProject;
        Artifact projectArtifact =
                repositorySystem.createProjectArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
        try {
            mavenProject = projectBuilder.build(projectArtifact, projectBuildingRequest).getProject();
        } catch (ProjectBuildingException e) {
            getLog()
                    .warn(format("The artifact [%s] seems to have some warnings, enable logs for more information", artifact.toString()));
            if (getLog().isDebugEnabled()) {
                getLog().warn(format("The artifact [%s] had the following issue ", artifact.toString()), e);
            }
            if (e.getResults() == null || e.getResults().size() != 1) {
                throw new MojoExecutionException(format("There was an issue while trying to create a maven project from the artifact [%s]",
                        artifact.toString()),
                        e);
            }
            final ProjectBuildingResult projectBuildingResult = e.getResults().get(0);
            final List<ModelProblem> collect = projectBuildingResult.getProblems().stream()
                    .filter(modelProblem -> modelProblem.getSeverity().equals(Severity.FATAL)).collect(
                            Collectors.toList());
            if (!collect.isEmpty()) {
                throw new MojoExecutionException(format("There was an issue while trying to create a maven project from the artifact [%s], several FATAL errors were found",
                        artifact.toString()),
                        e);
            }
            mavenProject = projectBuildingResult.getProject();
        }
        return mavenProject;
    }

    private void addParentDependencyPomArtifacts(MavenProject projectDependency, Set<Artifact> artifacts)
            throws MojoExecutionException {
        MavenProject currentProject = projectDependency;
        while (currentProject.hasParent()) {
            currentProject = currentProject.getParent();
            final Artifact pomArtifact = currentProject.getArtifact();
            if (!artifacts.add(getResolvedArtifactUsingLocalRepository(pomArtifact))) {
                break;
            }
        }
    }

    private void addParentPomArtifacts(Set<Artifact> artifacts)
            throws MojoExecutionException {
        MavenProject currentProject = project;
        boolean projectParent = true;
        while (currentProject.hasParent() && projectParent) {
            currentProject = currentProject.getParent();
            if (currentProject.getFile() == null) {
                projectParent = false;
            } else {
                Artifact pomArtifact = currentProject.getArtifact();
                pomArtifact.setFile(currentProject.getFile());
                validatePomArtifactFile(pomArtifact);
                if (!artifacts.add(pomArtifact)) {
                    break;
                }
            }
        }
        if (!projectParent) {
            final Artifact unresolvedParentPomArtifact = currentProject.getArtifact();
            addThirdPartyParentPomArtifacts(artifacts, unresolvedParentPomArtifact);
        }
    }

    private Artifact getResolvedArtifactUsingLocalRepository(Artifact pomArtifact) throws MojoExecutionException {
        final Artifact resolvedPomArtifact = localRepository.find(pomArtifact);
        validatePomArtifactFile(resolvedPomArtifact);
        return resolvedPomArtifact;
    }

    private void validatePomArtifactFile(Artifact resolvedPomArtifact) throws MojoExecutionException {
        if (resolvedPomArtifact.getFile() == null) {
            throw new MojoExecutionException(format("There was a problem trying to resolve the artifact's file location for [%s], file was null",
                    resolvedPomArtifact.toString()));
        }
        if (!resolvedPomArtifact.getFile().exists()) {
            throw new MojoExecutionException(format("There was a problem trying to resolve the artifact's file location for [%s], file [%s] doesn't exists",
                    resolvedPomArtifact.toString(), resolvedPomArtifact.getFile().getAbsolutePath()));
        }
    }

    private String getFormattedFileName(Artifact artifact) {
        StringBuilder destFileName = new StringBuilder();
        String versionString = "-" + getNormalizedVersion(artifact);
        String classifierString = "";

        if (artifact.getClassifier() != null && !artifact.getClassifier().isEmpty()) {
            classifierString = "-" + artifact.getClassifier();
        }
        destFileName.append(artifact.getArtifactId()).append(versionString);
        destFileName.append(classifierString).append(".");
        destFileName.append(artifact.getArtifactHandler().getExtension());

        return destFileName.toString();
    }

    private String getNormalizedVersion(Artifact artifact) {
        if (artifact.isSnapshot() && !artifact.getVersion().equals(artifact.getBaseVersion())) {
            return artifact.getBaseVersion();
        }
        return artifact.getVersion();
    }

    private static File getFormattedOutputDirectory(File outputDirectory, Artifact artifact) {
        StringBuilder sb = new StringBuilder(128);
        sb.append(artifact.getGroupId().replace('.', separatorChar)).append(separatorChar);
        sb.append(artifact.getArtifactId()).append(separatorChar);
        sb.append(artifact.getBaseVersion()).append(separatorChar);

        return new File(outputDirectory, sb.toString());
    }
}
