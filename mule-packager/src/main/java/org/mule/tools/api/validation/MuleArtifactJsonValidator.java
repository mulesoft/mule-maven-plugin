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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.mule.runtime.api.deployment.meta.Product.MULE;
import static org.mule.runtime.api.deployment.meta.Product.MULE_EE;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;

public class MuleArtifactJsonValidator {

  /**
   * It validates that the mandatory descriptor file is present and is valid.
   *
   * @throws ValidationException if the project descriptor file is missing and/or is invalid
   * @param projectBaseDir
   */
  public static void validate(Path projectBaseDir) throws ValidationException {
    isMuleArtifactJsonPresent(projectBaseDir);
    isMuleArtifactJsonValid(projectBaseDir);
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
   */
  public static void isMuleArtifactJsonValid(Path projectBaseDir) throws ValidationException {
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
    validateMuleArtifactMandatoryFields(muleArtifact);
  }

  /**
   * Validates that the mandatory fields in the mule-artifact.json file are present and have valid values.
   *
   * @throws ValidationException if the project descriptor file does not have set all mandatory fields
   * @param muleArtifact
   */
  protected static void validateMuleArtifactMandatoryFields(MuleApplicationModel muleArtifact) throws ValidationException {
    List<String> missingFields = new ArrayList<>();

    checkName(muleArtifact, missingFields);
    checkMinMuleVersionValue(muleArtifact, missingFields);
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
  protected static void checkMinMuleVersionValue(MuleApplicationModel muleArtifact, List<String> missingFields) {
    String minMuleVersionValue = muleArtifact.getMinMuleVersion();
    if (StringUtils.isBlank(minMuleVersionValue)) {
      missingFields.add("minMuleVersion");
    }
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
