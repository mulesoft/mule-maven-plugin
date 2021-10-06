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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.tools.api.packager.sources.MuleArtifactContentResolver;
import org.mule.tools.api.packager.sources.MuleContentGenerator;
import org.mule.tools.api.packager.structure.ProjectStructure;
import org.mule.tooling.api.AstGenerator;

/**
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
@Mojo(name = "compile",
    defaultPhase = LifecyclePhase.COMPILE,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class CompileMojo extends AbstractMuleMojo {

  private static final String RUNTIME_AST_VERSION = "4.4.0";
  private static final String MULE_POLICY = "mule-policy";
  private static final String MULE_DOMAIN = "mule-domain";
  private static final String SKIP_AST = "skipAST";

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
    } catch (IllegalArgumentException | IOException e) {
      throw new MojoFailureException("Fail to compile", e);
    }
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

  public ArtifactAst getArtifactAst() throws FileNotFoundException, IOException {
    AstGenerator astGenerator = new AstGenerator(getAetherMavenClient(), RUNTIME_AST_VERSION,
                                                 project.getDependencies(), projectBaseFolder.toPath().resolve("target"));
    ProjectStructure projectStructure = new ProjectStructure(projectBaseFolder.toPath(), false);
    MuleArtifactContentResolver contentResolver =
        new MuleArtifactContentResolver(new ProjectStructure(projectBaseFolder.toPath(), false),
                                        getProjectInformation().getEffectivePom(),
                                        getProjectInformation().getProject().getBundleDependencies());

    return astGenerator.generateAST(contentResolver.getConfigs(), projectStructure.getConfigsPath());
  }

}
