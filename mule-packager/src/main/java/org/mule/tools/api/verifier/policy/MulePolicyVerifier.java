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

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;

import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.verifier.ProjectVerifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Ensures the project is valid
 */
public class MulePolicyVerifier implements ProjectVerifier {

  public static final String TEMPLATE_XML = join(File.separator, "src", "main", "mule", "template.xml");
  public static final String POM_XML = "pom.xml";
  public static final String MULE_ARTIFACT_JSON = "mule-artifact.json";
  private final String yamlFileName;
  private final ProjectInformation projectInformation;

  public MulePolicyVerifier(ProjectInformation projectInformation) {
    this.projectInformation = projectInformation;
    yamlFileName = format("%s.yaml", projectInformation.getArtifactId());
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
    try {
      ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
      mapper.configure(FAIL_ON_NULL_CREATOR_PROPERTIES, true);
      mapper.readValue(new File(getBaseDir(), yamlFileName), PolicyYaml.class);
    } catch (IOException e) {
      throw new ValidationException(format("Error validating '%s'. %s", yamlFileName, e.getMessage()));
    }
  }

  private void validateJson() throws ValidationException {
    PolicyMuleArtifactJsonVerifier.validate(projectInformation, getMuleArtifactFile());
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

  private static void fileExists(String baseDir, String fileName) throws ValidationException {
    File file = new File(baseDir, fileName);
    if (!file.exists()) {
      throw new ValidationException(format("The file %s should be present.", fileName));
    }
  }



}
