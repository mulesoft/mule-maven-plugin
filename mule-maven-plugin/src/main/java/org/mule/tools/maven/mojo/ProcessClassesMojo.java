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

import static org.mule.tools.api.packager.packaging.Classifier.MULE_PLUGIN;
import static org.mule.tools.maven.mojo.model.lifecycle.MavenLifecyclePhase.VALIDATE;

import org.mule.runtime.api.meta.MuleVersion;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.validation.ValidationResultItem;
import org.mule.tooling.api.AstGenerator;
import org.mule.tooling.api.AstValidatonResult;
import org.mule.tooling.api.ConfigurationException;
import org.mule.tooling.api.DynamicStructureException;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.sources.MuleArtifactContentResolver;
import org.mule.tools.api.packager.sources.MuleContentGenerator;
import org.mule.tools.api.packager.structure.ProjectStructure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Post process the generated files from compilation, which in this case will be the mule-artifact.json from the compiled java
 * classes plus any other resource already copied to the output directory.
 */
@Mojo(name = "process-classes",
    defaultPhase = LifecyclePhase.PROCESS_CLASSES,
    requiresDependencyResolution = ResolutionScope.TEST)
public class ProcessClassesMojo extends AbstractMuleMojo {

  @Component
  private PluginDescriptor descriptor;
  private static final MuleVersion MIN_RUNTIME_AST_VERSION = new MuleVersion("4.4.0");
  private static final String MULE_POLICY = "mule-policy";
  private static final String MULE_DOMAIN = "mule-domain";
  private static final String SKIP_AST = "skipAST";
  private static final String SKIP_AST_VALIDATION = "skipASTValidation";

  @Override
  public void doExecute() throws MojoExecutionException, MojoFailureException {
    getLog().debug("Generating process-classes code...");
    try {

      String skipAST = System.getProperty(SKIP_AST);
      // apps in domains are not currently supported MMP-588
      if ((skipAST == null || skipAST.equals("false")) && !project.getPackaging().equals(MULE_POLICY) && !hasDomain()) {
        processAst();
      }
    } catch (IllegalArgumentException | IOException | ConfigurationException e) {
      throw new MojoFailureException("Fail to compile", e);
    }
    try {
      getContentGenerator().copyDescriptorFile();
      if (!skipValidation) {
        getLog().debug("executing validations in process-classes for Mule application");
        getProjectValidator().isProjectValid(VALIDATE.id());
      } else {
        getLog().debug("Skipping process-classes validation for Mule application");
      }
    } catch (ValidationException | IOException e) {
      throw new MojoExecutionException("process-classes exception", e);
    }
  }

  private void processAst() throws IOException, ConfigurationException, MojoExecutionException {
    ArtifactAst artifact;
    try {
      artifact = getArtifactAst();
    } catch (DynamicStructureException e) {
      getLog().warn("The application has a dynamic structure based on properties available only at design time,"
          + " so an artifact AST for it cannot be generated at this time."
          + " See previous WARN messages for where that dynamic structure is being detected.");
      return;
    }

    if (artifact != null) {
      ((MuleContentGenerator) getContentGenerator()).createAstFile(serialize(artifact));
    }
  }

  @Override
  public String getPreviousRunPlaceholder() {
    return "MULE_MAVEN_PLUGIN_PROCESS_CLASSES_PREVIOUS_RUN_PLACEHOLDER";
  }

  public ArtifactAst getArtifactAst()
      throws IOException, ConfigurationException, DynamicStructureException, MojoExecutionException {
    descriptor.getClassRealm()
        .addURL(project.getBasedir().toPath().resolve("src").resolve("main").resolve("resources").toUri().toURL());
    MuleVersion appMinRuntimeVersion = new MuleVersion(this.getMuleApplicationModelLoader().getRuntimeVersion());
    MuleVersion runtimeVersion =
        appMinRuntimeVersion.newerThan(MIN_RUNTIME_AST_VERSION) ? appMinRuntimeVersion : MIN_RUNTIME_AST_VERSION;
    MuleArtifactContentResolver contentResolver =
        new MuleArtifactContentResolver(new ProjectStructure(projectBaseFolder.toPath(), false),
                                        getProjectInformation().getEffectivePom(),
                                        getProjectInformation().getProject().getBundleDependencies());
    AstGenerator astGenerator = new AstGenerator(getMavenClient(), runtimeVersion.toString(),
                                                 project.getArtifacts(), Paths.get(project.getBuild().getDirectory()),
                                                 descriptor.getClassRealm(), project.getDependencies(),
                                                 contentResolver.isApplication());
    ProjectStructure projectStructure = new ProjectStructure(projectBaseFolder.toPath(), false);

    ArtifactAst artifactAST = astGenerator.generateAST(contentResolver.getConfigs(), projectStructure.getConfigsPath());
    String skipASTValidation = System.getProperty(SKIP_AST_VALIDATION);
    if (artifactAST != null && !this.getClassifier().equalsIgnoreCase(MULE_PLUGIN.toString())
        && (skipASTValidation == null || skipASTValidation.equals("false"))) {
      AstValidatonResult validationResult = astGenerator.validateAST(artifactAST);
      for (ValidationResultItem warning : validationResult.getWarnings()) {
        getLog().warn(warning.getMessage());
      }
      if (!validationResult.getDynamicStructureErrors().isEmpty()) {
        for (ValidationResultItem dynamicStructureError : validationResult.getDynamicStructureErrors()) {
          getLog().warn(dynamicStructureError.getMessage());
        }
        throw new DynamicStructureException();
      }
    }
    return artifactAST;
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

}
