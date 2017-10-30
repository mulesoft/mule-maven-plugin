/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.validation;

import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.api.deployment.meta.Product;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.model.Deployment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;
import static org.mule.tools.api.validation.AbstractProjectValidator.getSeparatorIndex;
import static org.mule.tools.api.validation.AbstractProjectValidator.isProjectVersionValid;

public class MuleArtifactJsonValidator {

  /**
   * It validates that the mandatory descriptor file is present and is valid.
   *
   * @throws ValidationException if the project descriptor file is missing and/or is invalid
   * @param projectBaseDir
   */
  public static void validate(Path projectBaseDir, Deployment deploymentConfiguration) throws ValidationException {
    isMuleArtifactJsonPresent(projectBaseDir);
    isMuleArtifactJsonValid(projectBaseDir, deploymentConfiguration);
  }

  /**
   * It validates that the mandatory descriptor file is present.
   *
   * @throws ValidationException if the project descriptor file(s) is/are missing
   * @param projectBaseDir
   */
  public static void isMuleArtifactJsonPresent(Path projectBaseDir) throws ValidationException {
    String errorMessage = "Invalid Mule project. Missing %s file, it must be present in the root of application";
    if (!projectBaseDir.resolve(MULE_ARTIFACT_JSON).toFile().exists()) {
      throw new ValidationException(String.format(errorMessage, MULE_ARTIFACT_JSON));
    }
  }

  /**
   * It validates that the mandatory descriptor file is valid. It assumes that the file exists.
   *
   * @throws ValidationException if the project descriptor file is invalid
   * @param projectBaseDir
   * @param deploymentConfiguration
   */
  public static void isMuleArtifactJsonValid(Path projectBaseDir, Deployment deploymentConfiguration)
      throws ValidationException {
    File muleArtifactJsonFile = projectBaseDir.resolve(MULE_ARTIFACT_JSON).toFile();
    MuleApplicationModel muleArtifact;
    try {
      muleArtifact =
          new MuleApplicationModelJsonSerializer().deserialize(FileUtils.readFileToString(muleArtifactJsonFile, (String) null));
      if (muleArtifact == null) {
        throw new ValidationException("The mule-artifact.json file is empty");
      }
    } catch (IOException | JsonSyntaxException e) {
      throw new ValidationException(e);
    }
    validateMuleArtifactMandatoryFields(muleArtifact, deploymentConfiguration);
  }

  /**
   * Validates that the mandatory fields in the mule-artifact.json file are present and have valid values.
   *
   * @throws ValidationException if the project descriptor file does not have set all mandatory fields
   * @param muleArtifact
   */
  protected static void validateMuleArtifactMandatoryFields(MuleApplicationModel muleArtifact,
                                                            Deployment deploymentConfiguration)
      throws ValidationException {
    List<String> missingFields = new ArrayList<>();

    checkName(muleArtifact, missingFields);
    checkMinMuleVersionValue(muleArtifact, missingFields, deploymentConfiguration);
    checkClassLoaderModelDescriptor(muleArtifact, missingFields);
    checkRequiredProduct(muleArtifact, missingFields);

    if (!missingFields.isEmpty()) {
      String message = "The following mandatory fields in the mule-artifact.json are missing or invalid: "
          + missingFields;
      if (missingFields.contains("requiredProduct")) {
        message += ". requiredProduct valid values are: MULE, MULE_EE";
      }
      throw new ValidationException(message);
    }
  }

  /**
   * Checks that the requiredProduct field is present in the mule artifact instance. If it is not defined or if it has a invalid
   * value, the field name is added to the missing fields list.
   *
   * @param muleArtifact the mule artifact to be checked
   * @param missingFields list of required fields that are missing
   */
  private static void checkRequiredProduct(MuleApplicationModel muleArtifact, List<String> missingFields) {
    Product requiredProduct = muleArtifact.getRequiredProduct();
    if (requiredProduct == null) {
      missingFields.add("requiredProduct");
    }
  }

  /**
   * Checks that the name field is present in the mule artifact instance. If it is not defined, the field name is added to the
   * missing fields list.
   *
   * @param muleArtifact the mule artifact to be checked
   * @param missingFields list of required fields that are missing
   */
  protected static void checkName(MuleApplicationModel muleArtifact, List<String> missingFields) {
    String nameValue = muleArtifact.getName();
    if (StringUtils.isBlank(nameValue)) {
      missingFields.add("name");
    }
  }

  /**
   * Checks that the minMuleVersion field is present in the mule artifact instance. If it is not defined, the field name is added
   * to the missing fields list.
   *
   * @param muleArtifact the mule artifact to be checked
   * @param missingFields list of required fields that are missing
   */
  protected static void checkMinMuleVersionValue(MuleApplicationModel muleArtifact, List<String> missingFields,
                                                 Deployment deploymentConfiguration)
      throws ValidationException {
    String minMuleVersionValue = muleArtifact.getMinMuleVersion();
    if (StringUtils.isBlank(minMuleVersionValue)) {
      missingFields.add("minMuleVersion");
    } else if (deploymentConfiguration != null && deploymentConfiguration.getMuleVersion().isPresent()
        && StringUtils.isNotBlank(deploymentConfiguration.getMuleVersion().get())) {
      areVersionCompatible(deploymentConfiguration.getMuleVersion().get(), minMuleVersionValue);
    }
  }

  /**
   * Verifies if mule version is contained in the interval [minMuleVersion,∞).
   *
   * @param muleVersion mule version that is going to be checked against the specified range.
   * @param minMuleVersion the infimum of the set of compatible mule versions.
   */
  private static void areVersionCompatible(String muleVersion, String minMuleVersion) throws ValidationException {
    isProjectVersionValid(minMuleVersion);
    isProjectVersionValid(muleVersion);
    String minMuleVersionBaseValue = getBaseVersion(minMuleVersion);
    String muleVersionBaseValue = getBaseVersion(muleVersion);
    if (!isVersionBiggerOrEqual(muleVersionBaseValue, minMuleVersionBaseValue)) {
      throw new ValidationException("Mule version that is set in the deployment configuration is not compatible with the minMuleVersion in mule-artifact.json. deploymentConfiguration.muleVersion: "
          + muleVersion + ", minMuleVersion: " + minMuleVersion);
    }
  }

  /**
   * Returns true if version1 >= version2.
   *
   * @param version1 String in the format X.Y.Z, where all values comply with the semantic versioning specification.
   * @param version2 String in the format X.Y.Z, where all values comply with the semantic versioning specification.
   */
  private static boolean isVersionBiggerOrEqual(String version1, String version2) {
    List<Integer> version1Split = Arrays.stream(version1.split("\\.")).map(Integer::parseInt).collect(Collectors.toList());
    List<Integer> version2Split = Arrays.stream(version2.split("\\.")).map(Integer::parseInt).collect(Collectors.toList());

    int major1 = version1Split.get(0);
    int minor1 = version1Split.get(1);
    int patch1 = version1Split.get(2);

    int major2 = version2Split.get(0);
    int minor2 = version2Split.get(1);
    int patch2 = version2Split.get(2);

    return major1 > major2 || (major1 == major2 && ((minor1 > minor2) || (minor1 == minor2 && patch1 >= patch2)));
  }

  private static String getBaseVersion(String version) {
    int separator = getSeparatorIndex(version);
    return separator == -1 ? version : version.substring(0, separator);
  }

  /**
   * Checks that the classLoaderModelDescriptor field is present in the mule artifact instance. If it is not defined, the field
   * name is added to the missing fields list. Also checks if the classLoaderModelDescriptor id is defined, and also adds it to
   * the missing fields in case it is not defined.
   *
   * @param muleArtifact the mule artifact to be checked
   * @param missingFields list of required fields that are missing
   */
  protected static void checkClassLoaderModelDescriptor(MuleApplicationModel muleArtifact, List<String> missingFields) {
    MuleArtifactLoaderDescriptor classLoaderModelDescriptor = muleArtifact.getClassLoaderModelLoaderDescriptor();
    if (classLoaderModelDescriptor == null) {
      missingFields.add("classLoaderModelLoaderDescriptor");
    }
    if (classLoaderModelDescriptor == null || StringUtils.isBlank(classLoaderModelDescriptor.getId())) {
      missingFields.add("classLoaderModelLoaderDescriptor.id");
    }
  }
}
