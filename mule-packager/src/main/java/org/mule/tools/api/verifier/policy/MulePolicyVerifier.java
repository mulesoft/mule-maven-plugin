/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.verifier.policy;

import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.verifier.ProjectVerifier;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;
import static org.mule.tools.api.packager.structure.PackagerFiles.POM_XML;

/**
 * Verifies that the packaged project is valid.
 * Checks that all necessary files are present.
 * Checks the validity of the yaml file.
 * Checks the validity of the mule-artifact.json
 */
public class MulePolicyVerifier implements ProjectVerifier {

  private static final String TEMPLATE_XML = join(File.separator, "src", "main", "mule", "template.xml");

  private final String yamlFileName;
  private final ProjectInformation projectInformation;

  public MulePolicyVerifier(ProjectInformation projectInformation) {
    this.projectInformation = projectInformation;
    this.yamlFileName = format("%s.yaml", projectInformation.getArtifactId());
  }

  @Override
  public void verify() throws ValidationException {
    allFilesPresent();
    validateYaml();
    validateJson();
  }

  private void allFilesPresent() throws ValidationException {
    for (String file : getNecessaryFiles()) {
      fileExists(getBaseDir(), file);
    }
  }

  private void validateYaml() throws ValidationException {
    new PolicyYamlVerifier(getBaseDir(), yamlFileName).validate();
  }

  private void validateJson() throws ValidationException {
    new PolicyMuleArtifactJsonVerifier(projectInformation, getMuleArtifactFile()).validate();
  }

  private File getMuleArtifactFile() {
    String base = join(
                       File.separator,
                       projectInformation.getBuildDirectory().toAbsolutePath().toString(),
                       "META-INF",
                       "mule-artifact");
    return new File(base, MULE_ARTIFACT_JSON);
  }

  private String getBaseDir() {
    return projectInformation.getProjectBaseFolder().toAbsolutePath().toString();
  }

  private List<String> getNecessaryFiles() {
    return asList(
                  POM_XML,
                  MULE_ARTIFACT_JSON,
                  TEMPLATE_XML,
                  yamlFileName);
  };

  private void fileExists(String baseDir, String fileName) throws ValidationException {
    File file = new File(baseDir, fileName);
    if (!file.exists()) {
      throw new ValidationException(format("The file %s should be present.", fileName));
    }
  }



}
