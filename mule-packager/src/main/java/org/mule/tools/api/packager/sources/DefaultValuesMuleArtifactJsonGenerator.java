/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.packager.sources;

import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import org.apache.commons.io.FileUtils;

import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;
import org.mule.tools.api.packager.structure.ProjectStructure;

/**
 * Generates default value for any non-defined fields in a mule-artifact.json file
 */
public class DefaultValuesMuleArtifactJsonGenerator {

  private static final MuleApplicationModelJsonSerializer serializer = new MuleApplicationModelJsonSerializer();
  private static final String MULE_ID = "mule";

  /**
   * Generates the default value for every non-defined fields in a mule-artifact.json file during build time and updates
   * accordingly
   *
   * @param muleArtifactContentResolver muleArtifactContentResolver of project being built
   */
  public static void generate(MuleArtifactContentResolver muleArtifactContentResolver) throws IOException {
    Path muleArtifactJsonLocation = resolveMuleArtifactJsonLocation(muleArtifactContentResolver.getProjectStructure());
    generate(muleArtifactJsonLocation, muleArtifactJsonLocation, muleArtifactContentResolver);
  }

  /**
   * Resolves the mule-artifact.json location based on the project base folder during build time
   *
   * @param projectStructure projectStructure of project being built
   */
  protected static Path resolveMuleArtifactJsonLocation(ProjectStructure projectStructure) {
    return projectStructure.getMuleArtifactJsonPath();
  }

  /**
   * Generates the default value for every non-defined fields in a mule-artifact.json and copy this to the destination folder
   *
   * @param originFolder                folder location containing the original mule-artifact.json
   * @param destinationFolder           folder location where the updated mule-artifact.json is going to be written to
   * @param muleArtifactContentResolver resolves all the contents that are going to be defaulted in the generated the
   *                                    mule-artifact.json
   */
  public static void generate(Path originFolder, Path destinationFolder,
                              MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    MuleApplicationModel originalMuleArtifact = getOriginalMuleArtifact(originFolder);

    MuleApplicationModel generatedMuleArtifact =
        generateMuleArtifactWithDefaultValues(originalMuleArtifact, muleArtifactContentResolver);

    writeMuleArtifactToFile(generatedMuleArtifact, destinationFolder);
  }

  /**
   * Writes the mule artifact to the corresponding file
   *
   * @param muleArtifact      mule artifact that is going to be persisted
   * @param destinationFolder destination folder where the mule-artifact.json file is going to be created
   */
  protected static void writeMuleArtifactToFile(MuleApplicationModel muleArtifact, Path destinationFolder)
      throws IOException {
    String generatedMuleArtifactJsonContent = serializer.serialize(muleArtifact);
    File generatedMuleArtifactJson = new File(destinationFolder.toFile(), MULE_ARTIFACT_JSON);
    FileUtils.writeStringToFile(generatedMuleArtifactJson, generatedMuleArtifactJsonContent, (String) null);
  }

  /**
   * Deserializes the mule-artifact.json to a {@link MuleApplicationModel}
   *
   * @param originFolder folder path where the mule-artifact.json is located
   */
  protected static MuleApplicationModel getOriginalMuleArtifact(Path originFolder) throws IOException {
    File originalMuleArtifactJsonFile = originFolder.resolve(MULE_ARTIFACT_JSON).toFile();
    return serializer.deserialize(FileUtils.readFileToString(originalMuleArtifactJsonFile, (String) null));
  }

  /**
   * Generates a {@link MuleApplicationModel} with default values based on a {@link MuleArtifactContentResolver}.
   *
   * @param originalMuleArtifact        original mule application model
   * @param muleArtifactContentResolver the application content resolver
   */
  protected static MuleApplicationModel generateMuleArtifactWithDefaultValues(MuleApplicationModel originalMuleArtifact,
                                                                              MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    MuleApplicationModel.MuleApplicationModelBuilder builder = getBuilderWithRequiredValues(originalMuleArtifact);
    setBuilderWithDefaultValuesNotPresent(builder, originalMuleArtifact, muleArtifactContentResolver);
    return builder.build();
  }

  /**
   * Updates a {@link MuleApplicationModel.MuleApplicationModelBuilder} with default values based on a
   * {@link MuleArtifactContentResolver}.
   *
   * @param builder                     builder for the generated mule artifact based on the original one
   * @param originalMuleArtifact        original mule application model
   * @param muleArtifactContentResolver the application content resolver
   */
  protected static void setBuilderWithDefaultValuesNotPresent(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                              MuleApplicationModel originalMuleArtifact,
                                                              MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    setBuilderWithDefaultRedeploymentEnabled(builder, originalMuleArtifact);
    setBuilderWithDefaultConfigsValue(builder, originalMuleArtifact, muleArtifactContentResolver);
    // setBuilderWithDefaultExportedPackagesValue(builder, muleArtifactContentResolver);
    setBuilderWithDefaultExportedResourcesValue(builder, originalMuleArtifact, muleArtifactContentResolver);
    setBuilderWithIncludeTestDependencies(builder, muleArtifactContentResolver);
  }

  protected static void setBuilderWithIncludeTestDependencies(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                              MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    MuleArtifactLoaderDescriptor descriptorLoader = builder.getClassLoaderModelDescriptorLoader();
    if (muleArtifactContentResolver.getProjectStructure().getTestConfigsPath().isPresent()) {
      Map<String, Object> attributesCopy = getUpdatedAttributes(descriptorLoader, "includeTestDependencies", "true");
      builder.withClassLoaderModelDescriptorLoader(new MuleArtifactLoaderDescriptor(descriptorLoader.getId(), attributesCopy));
    }
  }

  /**
   * Updates a {@link MuleApplicationModel.MuleApplicationModelBuilder} exportedResources in the attributes field with default
   * values based on a {@link MuleArtifactContentResolver}.
   *
   * @param builder                     builder for the mule artifact that is going to be generated
   * @param muleArtifactContentResolver the application content resolver
   */
  protected static void setBuilderWithDefaultExportedResourcesValue(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                                    MuleApplicationModel originalMuleArtifact,
                                                                    MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    MuleArtifactLoaderDescriptor descriptorLoader = builder.getClassLoaderModelDescriptorLoader();


    Map<String, Object> originalAttributes = originalMuleArtifact.getClassLoaderModelLoaderDescriptor().getAttributes();
    List<String> exportedResources = new ArrayList<>();

    if (originalAttributes != null && originalAttributes.get("exportedResources") != null) {
      exportedResources.addAll((Collection<String>) originalAttributes.get("exportedResources"));
    } else {
      exportedResources.addAll(muleArtifactContentResolver.getExportedResources());
      exportedResources.addAll(muleArtifactContentResolver.getTestExportedResources());
    }

    Map<String, Object> attributesCopy =
        getUpdatedAttributes(descriptorLoader, "exportedResources", new ArrayList<>(exportedResources));
    builder.withClassLoaderModelDescriptorLoader(new MuleArtifactLoaderDescriptor(descriptorLoader.getId(), attributesCopy));
  }

  /**
   * Updates a {@link MuleApplicationModel.MuleApplicationModelBuilder} exportedPackages in the attributes field with default
   * values based on a {@link MuleArtifactContentResolver}.
   *
   * @param builder                     builder for the mule artifact that is going to be generated
   * @param muleArtifactContentResolver the application content resolver
   */
  protected static void setBuilderWithDefaultExportedPackagesValue(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                                   MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {

    MuleArtifactLoaderDescriptor descriptorLoader = builder.getClassLoaderModelDescriptorLoader();
    Map<String, Object> attributesCopy =
        getUpdatedAttributes(descriptorLoader, "exportedPackages", muleArtifactContentResolver.getExportedPackages());
    builder.withClassLoaderModelDescriptorLoader(new MuleArtifactLoaderDescriptor(descriptorLoader.getId(), attributesCopy));
  }

  private static Map<String, Object> getUpdatedAttributes(MuleArtifactLoaderDescriptor descriptorLoader, String attribute,
                                                          Object value) {
    Map<String, Object> originalAttributes = descriptorLoader.getAttributes();
    Map<String, Object> attributesCopy = new HashMap<>();
    if (originalAttributes != null) {
      attributesCopy.putAll(originalAttributes);
    }
    attributesCopy.put(attribute, value);
    return attributesCopy;
  }

  /**
   * Updates a {@link MuleApplicationModel.MuleApplicationModelBuilder} configs field with default values based on a
   * {@link MuleArtifactContentResolver}.
   *
   * @param builder                     builder for the mule artifact that is going to be generated
   * @param originalMuleArtifact        original mule application model
   * @param muleArtifactContentResolver the application content resolver
   */
  protected static void setBuilderWithDefaultConfigsValue(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                          MuleApplicationModel originalMuleArtifact,
                                                          MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {

    Set<String> configs = new HashSet<>();
    if (originalMuleArtifact.getConfigs() != null) {
      configs.addAll(originalMuleArtifact.getConfigs());
    } else {
      configs.addAll(muleArtifactContentResolver.getConfigs());
    }

    configs.addAll(muleArtifactContentResolver.getTestConfigs());

    builder.setConfigs(configs);
  }

  /**
   * Updates a {@link MuleApplicationModel.MuleApplicationModelBuilder} redeploymentEnabled field with default the default value
   * based on the {@link MuleApplicationModel}.
   *
   * @param builder              builder for the mule artifact that is going to be generated
   * @param originalMuleArtifact the application content resolver
   */
  protected static void setBuilderWithDefaultRedeploymentEnabled(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                                 MuleApplicationModel originalMuleArtifact) {
    builder.setRedeploymentEnabled(originalMuleArtifact.isRedeploymentEnabled());
  }

  /**
   * Updates a {@link MuleApplicationModel.MuleApplicationModelBuilder} bundleDescriptorLoader field with default the default
   * value based on the {@link MuleApplicationModel}.
   *
   * @param builder              builder for the mule artifact that is going to be generated
   * @param originalMuleArtifact the application content resolver
   */
  protected static void setBuilderWithDefaultBundleDescriptorLoaderValue(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                                         MuleApplicationModel originalMuleArtifact) {
    MuleArtifactLoaderDescriptor bundleDescriptorLoader = originalMuleArtifact.getBundleDescriptorLoader();
    builder.withBundleDescriptorLoader(new MuleArtifactLoaderDescriptor(MULE_ID,
                                                                        bundleDescriptorLoader == null
                                                                            || bundleDescriptorLoader.getAttributes() == null
                                                                                ? new HashMap()
                                                                                : bundleDescriptorLoader.getAttributes()));
  }

  /**
   * Builds a {@link MuleApplicationModel.MuleApplicationModelBuilder} with the required values of a {@link MuleApplicationModel}.
   *
   * @param muleArtifact mule application model that is going to be used to generate the builder
   */
  protected static MuleApplicationModel.MuleApplicationModelBuilder getBuilderWithRequiredValues(MuleApplicationModel muleArtifact) {
    MuleApplicationModel.MuleApplicationModelBuilder builder = new MuleApplicationModel.MuleApplicationModelBuilder();
    builder.setName(muleArtifact.getName());
    builder.withClassLoaderModelDescriptorLoader(muleArtifact.getClassLoaderModelLoaderDescriptor());
    builder.setMinMuleVersion(muleArtifact.getMinMuleVersion());
    builder.setRequiredProduct(muleArtifact.getRequiredProduct());
    builder.setSecureProperties(muleArtifact.getSecureProperties());
    setBuilderWithDefaultBundleDescriptorLoaderValue(builder, muleArtifact);
    return builder;
  }
}
