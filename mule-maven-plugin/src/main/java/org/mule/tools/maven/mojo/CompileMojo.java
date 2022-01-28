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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.validation.ValidationResultItem;
import org.mule.tools.api.packager.sources.MuleArtifactContentResolver;
import org.mule.tools.api.packager.sources.MuleContentGenerator;
import org.mule.tools.api.packager.structure.ProjectStructure;
import org.mule.tooling.api.AstGenerator;
import org.mule.tooling.api.ConfigurationException;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
@Mojo(name = "compile",
    defaultPhase = LifecyclePhase.COMPILE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class CompileMojo extends AbstractMuleMojo {

  @Component
  private PluginDescriptor descriptor;

  private static final String RUNTIME_AST_VERSION = "4.4.0";
  private static final String MULE_POLICY = "mule-policy";
  private static final String MULE_DOMAIN = "mule-domain";
  private static final String SKIP_AST = "skipAST";
  public static final String EXT_MODEL_LOADER_DEPENDENCIES_TARGET = "jars";
  private static final String EXT_MODEL_LOADER_DEPENDENCIES_FOLDER = "alternateLocation";

  @Override
  public void doExecute() throws MojoFailureException {
    getLog().debug("Generating mule source code...");
    try {
      ((MuleContentGenerator) getContentGenerator()).createMuleSrcFolderContent();
      String skipAST = System.getProperty(SKIP_AST);
      // apps in domains are not currently supported MMP-588
      if ((skipAST == null || skipAST.equals("false")) && !project.getPackaging().equals(MULE_POLICY) && !hasDomain()) {
        ArtifactAst artifact = getArtifactAst();
        if (artifact != null) {
          ((MuleContentGenerator) getContentGenerator()).createAstFile(serialize(artifact));
        }
      }
    } catch (IllegalArgumentException | IOException | ConfigurationException e) {
      throw new MojoFailureException("Fail to compile", e);
    }
  }

  private void addJarsToClasspath() throws IOException {
    Path targetDirPath = Paths.get(System.getProperty("java.io.tmpdir")).resolve("mmp-"
        + project.getPluginArtifactMap().get("org.mule.tools.maven:mule-maven-plugin").getVersion() + "-ast-deps");
    File targetFile = targetDirPath.toFile();
    if (!targetFile.exists()) {
      targetFile.mkdir();
    }
    File dependenciesDir = targetDirPath.resolve(EXT_MODEL_LOADER_DEPENDENCIES_FOLDER).toFile();
    extractDependencies(targetDirPath);
    File[] jarDeps = dependenciesDir.listFiles(file -> file.getAbsolutePath().endsWith("jar"));
    for (File file : jarDeps) {
      descriptor.getClassRealm().addURL(file.toURI().toURL());
    }
  }

  private void extractDependencies(Path targetDirPath) throws IOException {
    org.apache.maven.artifact.Artifact mmmp = project.getPluginArtifactMap().get("org.mule.tools.maven:mule-maven-plugin");
    Path extensionModelLoaderDepsPath =
        Paths.get((session.getLocalRepository().getBasedir())).resolve("org").resolve("mule").resolve("tools")
            .resolve("maven")
            .resolve("mule-extension-model-loader")
            .resolve(mmmp.getVersion())
            .resolve("mule-extension-model-loader-" + mmmp.getVersion() + "-dependencies.jar");
    ZipFile dependenciesJar = new ZipFile(extensionModelLoaderDepsPath.toFile());
    List<? extends ZipEntry> entries = dependenciesJar.stream()
        .sorted(Comparator.comparing(ZipEntry::getName))
        .collect(Collectors.toList());
    for (ZipEntry entry : entries) {
      Path entryDest = targetDirPath.resolve(entry.getName());
      if (!Files.exists(entryDest)) {
        if (entry.isDirectory()) {
          Files.createDirectory(entryDest);
          continue;
        }
        Files.copy(dependenciesJar.getInputStream(entry), entryDest);
      }
    }
    dependenciesJar.close();
  }

  private boolean hasDomain() {
    if (project.getDependencies() != null) {
      for (Dependency dependency : project.getDependencies()) {
        if (dependency.getClassifier() != null && dependency.getClassifier().equals(MULE_DOMAIN)) {
          return true;
        }
      }
    }
    return false;
  }

  public InputStream serialize(ArtifactAst artifact) {
    return AstGenerator.serialize(artifact);
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_COMPILE_PREVIOUS_RUN_PLACEHOLDER";
  }

  public ArtifactAst getArtifactAst() throws IOException, ConfigurationException {
    addJarsToClasspath();

    AstGenerator astGenerator = new AstGenerator(getAetherMavenClient(), RUNTIME_AST_VERSION,
                                                 project.getDependencies(), Paths.get(project.getBuild().getDirectory()));
    ProjectStructure projectStructure = new ProjectStructure(projectBaseFolder.toPath(), false);
    MuleArtifactContentResolver contentResolver =
        new MuleArtifactContentResolver(new ProjectStructure(projectBaseFolder.toPath(), false),
                                        getProjectInformation().getEffectivePom(),
                                        getProjectInformation().getProject().getBundleDependencies());

    ArtifactAst artifactAST = astGenerator.generateAST(contentResolver.getConfigs(), projectStructure.getConfigsPath());
    if (artifactAST != null) {
      ArrayList<ValidationResultItem> warnings = astGenerator.validateAST(artifactAST);
      for (ValidationResultItem warning : warnings) {
        getLog().warn(warning.getMessage());
      }
    }
    return artifactAST;
  }
}
