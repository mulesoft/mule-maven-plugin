/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.project.policy;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_POLICY;

import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;
import org.mule.tools.api.exception.ValidationException;
import org.mule.tools.api.packager.ProjectInformation;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;

class PolicyMuleArtifactJsonValidator {

  private static final String GROUP_ID = "groupId";
  private static final String ARTIFACT_ID = "artifactId";
  private static final String VERSION = "version";
  private static final String CLASSIFIER = "classifier";
  private static final String TYPE = "type";
  private static final String JAR_TYPE = "jar";
  private static final String EXPORTED_PACKAGES = "exportedPackages";
  private static final String EXPORTED_RESOURCES = "exportedResources";
  private static final String CONFIGS = "configs";

  public static void validate(ProjectInformation projectInformation, File file) throws ValidationException {
    MuleApplicationModel muleArtifact = getMuleArtifact(file);
    checkAttributesPresent(projectInformation, file, muleArtifact);
    checkNoInvalidFields(projectInformation, file, muleArtifact);
  }

  private static void checkNoInvalidFields(ProjectInformation projectInformation, File file,
                                           MuleApplicationModel muleApplicationModel)
      throws ValidationException {
    final Map<String, Object> attributes = muleApplicationModel.getClassLoaderModelLoaderDescriptor().getAttributes();
    try {
      checkArgument(attributes.get(EXPORTED_PACKAGES) == null, mustNotDefineFieldMessage(EXPORTED_PACKAGES));
      checkArgument(attributes.get(EXPORTED_RESOURCES) == null, mustNotDefineFieldMessage(EXPORTED_RESOURCES));
      checkArgument(muleApplicationModel.getConfigs() == null, mustNotDefineFieldMessage(CONFIGS));
    } catch (Exception e) {
      throw new ValidationException(format("Error in file '%s'. %s", file.getName(), e.getMessage()));
    }
  }

  private static void checkAttributesPresent(ProjectInformation projectInformation, File file,
                                             MuleApplicationModel muleApplicationModel)
      throws ValidationException {
    final Map<String, Object> attributes = muleApplicationModel.getBundleDescriptorLoader().getAttributes();
    try {
      checkArgument(projectInformation.getGroupId().equals(attributes.get(GROUP_ID)),
                    mismatchFieldWithPomMessage(GROUP_ID, projectInformation.getGroupId()));
      checkArgument(projectInformation.getArtifactId().equals(attributes.get(ARTIFACT_ID)),
                    mismatchFieldWithPomMessage(ARTIFACT_ID, projectInformation.getArtifactId()));
      checkArgument(projectInformation.getVersion().equals(attributes.get(VERSION)),
                    mismatchFieldWithPomMessage(VERSION, projectInformation.getVersion()));
      checkArgument(MULE_POLICY.toString().equals(attributes.get(CLASSIFIER)),
                    mismatchExpectedFieldValue(CLASSIFIER, MULE_POLICY.toString()));
      checkArgument(JAR_TYPE.equals(attributes.getOrDefault(TYPE, JAR_TYPE)), mismatchExpectedFieldValue(TYPE, JAR_TYPE));
    } catch (Exception e) {
      throw new ValidationException(format("Error validating attributes from '%s'. %s", file.getName(), e.getMessage()));
    }
  }

  private static MuleApplicationModel getMuleArtifact(File file) throws ValidationException {
    try {
      return new MuleApplicationModelJsonSerializer().deserialize(FileUtils.readFileToString(file, (String) null));
    } catch (IOException e) {
      throw new ValidationException(format("Error validating '%s'. %s", file.getName(), e.getMessage()));
    }
  }

  private static String mustNotDefineFieldMessage(String field) {
    return format("The field %s must not be defined.", field);
  }

  private static String mismatchFieldWithPomMessage(String field, String expectedValue) {
    return format("The %s does not match the one defined in the pom.xml. Expected '%s'.", field, expectedValue);
  }

  private static String mismatchExpectedFieldValue(String fieldName, String expectedValue) {
    return format("The field '%s' had an unexpected value. Expected '%s'.", fieldName, expectedValue);
  }


}
