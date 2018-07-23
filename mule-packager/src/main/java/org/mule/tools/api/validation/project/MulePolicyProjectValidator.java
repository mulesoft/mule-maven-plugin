/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.validation.project;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Arrays.asList;

import org.mule.tools.api.classloader.model.SharedLibraryDependency;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.ProjectInformation;
import org.mule.tools.api.validation.yaml.PolicyYaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Ensures the project is valid
 */
public class MulePolicyProjectValidator extends MuleProjectValidator {

  public static final String TEMPLATE_XML = join(File.separator, "src", "main", "mule", "template.xml");
  public static final String POM_XML = "pom.xml";
  public static final String MULE_ARTIFACT_JSON = "mule-artifact.json";
  public static final String EXCHANGE_TEMPLATE_POM_XML = "exchange-template-pom.xml";
  private final String yamlFileName;

  public MulePolicyProjectValidator(ProjectInformation projectInformation, List<SharedLibraryDependency> sharedLibraries,
                                    boolean strictCheck) {
    super(projectInformation, sharedLibraries, strictCheck);
    yamlFileName = format("%s.yaml", projectInformation.getArtifactId());
  }

  @Override
  protected void additionalValidation() throws ValidationException {
    isPolicyProjectStructureValid();
    super.additionalValidation();

  }

  public void isPolicyProjectStructureValid() throws ValidationException {
    allFilesPresent();
    validateYaml();
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

  private String getBaseDir() {
    return projectInformation.getProjectBaseFolder().toAbsolutePath().toString();
  }

  private List<String> getNecessaryFiles() {
    return asList(
                  POM_XML,
                  MULE_ARTIFACT_JSON,
                  EXCHANGE_TEMPLATE_POM_XML,
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
