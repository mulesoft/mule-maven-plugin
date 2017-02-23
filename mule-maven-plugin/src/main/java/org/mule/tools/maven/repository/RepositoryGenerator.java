/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.repository;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.sort;

public class RepositoryGenerator {

    public static final String REPOSITORY_FOLDER = "repository";

    private Log log;

    private MavenSession session;
    private MavenProject project;
    private ProjectBuilder projectBuilder;
    private RepositorySystem repositorySystem;
    private ArtifactRepository localRepository;
    private List<ArtifactRepository> remoteArtifactRepositories;

    protected File outputDirectory;
    private ProjectBuildingRequest projectBuildingRequest;

    public RepositoryGenerator(MavenSession session,
                               MavenProject project,
                               ProjectBuilder projectBuilder,
                               RepositorySystem repositorySystem,
                               ArtifactRepository localRepository,
                               List<ArtifactRepository> remoteArtifactRepositories,
                               File outputDirectory, Log log) {

        // TODO all this is mandatory
        this.log = log;
        this.session = session;
        this.project = project;
        this.projectBuilder = projectBuilder;
        this.repositorySystem = repositorySystem;
        this.localRepository = localRepository;
        this.remoteArtifactRepositories = remoteArtifactRepositories;
        this.outputDirectory = outputDirectory;
    }


    public void generate() throws MojoExecutionException, MojoFailureException {
        log.info(format("Mirroring repository for [%s]", project.toString()));
        try {
            initializeProjectBuildingRequest();

            ArtifactLocator artifactLocator =
                new ArtifactLocator(log, project, projectBuilder, repositorySystem, localRepository, projectBuildingRequest);
            Set<Artifact> artifacts = artifactLocator.getArtifacts();

            File repositoryFolder = getRepositoryFolder();

            installArtifacts(repositoryFolder, artifacts);
        } catch (Exception e) {
            log.debug(format("There was an exception while building [%s]", project.toString()), e);
            throw e;
        }
    }

    private void initializeProjectBuildingRequest() {
        projectBuildingRequest = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
        projectBuildingRequest.setLocalRepository(localRepository);
        projectBuildingRequest.setRemoteRepositories(remoteArtifactRepositories);


        log.debug(format("Local repository [%s]", projectBuildingRequest.getLocalRepository().getBasedir()));
        projectBuildingRequest.getRemoteRepositories().stream()
            .forEach(artifactRepository ->
                         log.debug(format("Remote repository ID [%s], URL [%s]", artifactRepository.getId(),
                                          artifactRepository.getUrl())));
    }

    private File getRepositoryFolder() {
        File repositoryFolder = new File(outputDirectory, REPOSITORY_FOLDER);
        if (!repositoryFolder.exists()) {
            repositoryFolder.mkdirs();
        }
        return repositoryFolder;
    }

//    private Set<Artifact> getArtifacts() throws MojoExecutionException {
//        Set<Artifact> artifacts = new HashSet<>(project.getArtifacts());
//        for (Artifact dep : new ArrayList<>(artifacts)) {
//            addThirdPartyParentPomArtifacts(artifacts, dep);
//        }
//        addParentPomArtifacts(artifacts);
//        return artifacts;
//    }

    private void installArtifacts(File repositoryFile, Set<Artifact> artifacts) throws MojoExecutionException {
        List<Artifact> sortedArtifacts = new ArrayList<>(artifacts);
        sort(sortedArtifacts);
        if (sortedArtifacts.isEmpty()) {
            generateMarkerFileInRepositoryFolder(repositoryFile, sortedArtifacts);
        }

        ArtifactInstaller installer = new ArtifactInstaller(log);
        for (Artifact artifact : sortedArtifacts) {
            //            installArtifact(repositoryFile, artifact);
            installer.installArtifact(repositoryFile, artifact);
        }
    }

    private void generateMarkerFileInRepositoryFolder(File repositoryFile, List<Artifact> sortedArtifacts)
        throws MojoExecutionException {
        File markerFile = new File(repositoryFile, ".marker");
        log.info(format("No artifacts to add, adding marker file <%s/%s>", REPOSITORY_FOLDER, markerFile.getName()));
        try {
            markerFile.createNewFile();
        } catch (IOException e) {
            throw new MojoExecutionException(
                format("The current repository has no artifacts to install, and trying to create [%s] failed",
                       markerFile.toString()),
                e);
        }
    }


//    private MavenProject buildProjectFromArtifact(Artifact artifact)
//        throws MojoExecutionException {
//        MavenProject mavenProject;
//        Artifact projectArtifact =
//            repositorySystem.createProjectArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
//        try {
//            mavenProject = projectBuilder.build(projectArtifact, projectBuildingRequest).getProject();
//        } catch (ProjectBuildingException e) {
//            log
//                .warn(format("The artifact [%s] seems to have some warnings, enable logs for more information",
//                             artifact.toString()));
//            if (log.isDebugEnabled()) {
//                log.warn(format("The artifact [%s] had the following issue ", artifact.toString()), e);
//            }
//            if (e.getResults() == null || e.getResults().size() != 1) {
//                throw new MojoExecutionException(
//                    format("There was an issue while trying to create a maven project from the artifact [%s]",
//                           artifact.toString()),
//                    e);
//            }
//            final ProjectBuildingResult projectBuildingResult = e.getResults().get(0);
//            final List<ModelProblem> collect = projectBuildingResult.getProblems().stream()
//                .filter(modelProblem -> modelProblem.getSeverity().equals(ModelProblem.Severity.FATAL)).collect(
//                    Collectors.toList());
//            if (!collect.isEmpty()) {
//                throw new MojoExecutionException(format(
//                    "There was an issue while trying to create a maven project from the artifact [%s], several FATAL errors were found",
//                    artifact.toString()),
//                                                 e);
//            }
//            mavenProject = projectBuildingResult.getProject();
//        }
//        return mavenProject;
//    }
//
//    private void addParentDependencyPomArtifacts(MavenProject projectDependency, Set<Artifact> artifacts)
//        throws MojoExecutionException {
//        MavenProject currentProject = projectDependency;
//        while (currentProject.hasParent()) {
//            currentProject = currentProject.getParent();
//            final Artifact pomArtifact = currentProject.getArtifact();
//            if (!artifacts.add(getResolvedArtifactUsingLocalRepository(pomArtifact))) {
//                break;
//            }
//        }
//    }

//    private void addParentPomArtifacts(Set<Artifact> artifacts)
//        throws MojoExecutionException {
//        MavenProject currentProject = project;
//        boolean projectParent = true;
//        while (currentProject.hasParent() && projectParent) {
//            currentProject = currentProject.getParent();
//            if (currentProject.getFile() == null) {
//                projectParent = false;
//            } else {
//                Artifact pomArtifact = currentProject.getArtifact();
//                pomArtifact.setFile(currentProject.getFile());
//                validatePomArtifactFile(pomArtifact);
//                if (!artifacts.add(pomArtifact)) {
//                    break;
//                }
//            }
//        }
//        if (!projectParent) {
//            final Artifact unresolvedParentPomArtifact = currentProject.getArtifact();
//            addThirdPartyParentPomArtifacts(artifacts, unresolvedParentPomArtifact);
//        }
//    }

//    private void addThirdPartyParentPomArtifacts(Set<Artifact> artifacts, Artifact dep) throws MojoExecutionException {
//        MavenProject project = buildProjectFromArtifact(dep);
//        addParentDependencyPomArtifacts(project, artifacts);
//
//        Artifact pomArtifact = repositorySystem.createProjectArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getVersion());
//        artifacts.add(getResolvedArtifactUsingLocalRepository(pomArtifact));
//    }

//    private Artifact getResolvedArtifactUsingLocalRepository(Artifact pomArtifact) throws MojoExecutionException {
//        final Artifact resolvedPomArtifact = localRepository.find(pomArtifact);
//        validatePomArtifactFile(resolvedPomArtifact);
//        return resolvedPomArtifact;
//    }

//    private void validatePomArtifactFile(Artifact resolvedPomArtifact) throws MojoExecutionException {
//        if (resolvedPomArtifact.getFile() == null) {
//            throw new MojoExecutionException(
//                format("There was a problem trying to resolve the artifact's file location for [%s], file was null",
//                       resolvedPomArtifact.toString()));
//        }
//        if (!resolvedPomArtifact.getFile().exists()) {
//            throw new MojoExecutionException(
//                format("There was a problem trying to resolve the artifact's file location for [%s], file [%s] doesn't exists",
//                       resolvedPomArtifact.toString(), resolvedPomArtifact.getFile().getAbsolutePath()));
//        }
//    }

} 
