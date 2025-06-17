/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.sources;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;

import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.deployment.meta.MuleArtifactLoaderDescriptor;
import org.mule.runtime.api.deployment.meta.Product;
import org.mule.runtime.api.deployment.persistence.MuleApplicationModelJsonSerializer;
import org.mule.tools.api.packager.Pom;
import org.mule.tools.api.packager.structure.ProjectStructure;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Generates default value for any non-defined fields in a mule-artifact.json file
 */
public abstract class AbstractDefaultValuesMuleArtifactJsonGenerator {

  protected static final String MULE_ID = "mule";
  protected static final MuleApplicationModelJsonSerializer serializer = new MuleApplicationModelJsonSerializer();


  /**
   * Generates the default value for every non-defined fields in a mule-artifact.json file during build time and updates
   * accordingly
   *
   * @param muleArtifactContentResolver muleArtifactContentResolver of project being built
   */
  public void generate(MuleArtifactContentResolver muleArtifactContentResolver) throws IOException {
    Path muleArtifactJsonLocation = resolveMuleArtifactJsonLocation(muleArtifactContentResolver.getProjectStructure());
    generate(muleArtifactJsonLocation, muleArtifactJsonLocation, muleArtifactContentResolver);
  }

  /**
   * Generates the default value for every non-defined fields in a mule-artifact.json and copy this to the destination folder
   *
   * @param originFolder folder location containing the original mule-artifact.json
   * @param destinationFolder folder location where the updated mule-artifact.json is going to be written to
   * @param muleArtifactContentResolver resolves all the contents that are going to be defaulted in the generated the
   *        mule-artifact.json
   */
  public void generate(Path originFolder, Path destinationFolder,
                       MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    MuleApplicationModelExtended originalMuleArtifactExtended = getOriginalMuleArtifact(originFolder);

    MuleApplicationModel generatedMuleArtifact =
        generateMuleArtifactWithDefaultValues(originalMuleArtifactExtended.getModel(), muleArtifactContentResolver);

    writeMuleArtifactExtendedToFile(new MuleApplicationModelExtended(generatedMuleArtifact,
                                                                     originalMuleArtifactExtended.getJavaSpecificationVersions()),
                                    destinationFolder);
  }

  /**
   * Resolves the mule-artifact.json location based on the project base folder during build time
   *
   * @param projectStructure projectStructure of project being built
   */
  private Path resolveMuleArtifactJsonLocation(ProjectStructure projectStructure) {
    return projectStructure.getMuleArtifactJsonPath();
  }

  /**
   * Writes the mule artifact to the corresponding file
   *
   * @param muleArtifact mule artifact that is going to be persisted
   * @param destinationFolder destination folder where the mule-artifact.json file is going to be created
   */
  private void writeMuleArtifactToFile(MuleApplicationModel muleArtifact, Path destinationFolder)
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
  private MuleApplicationModelExtended getOriginalMuleArtifact(Path originFolder) throws IOException {
    File originalMuleArtifactJsonFile = originFolder.resolve(MULE_ARTIFACT_JSON).toFile();
    String jsonContent = FileUtils.readFileToString(originalMuleArtifactJsonFile, (String) null);
    Set<String> javaSpecificationVersions;
    // Extract javaSpecificationVersions property independently because is not part of the model
    try {
      JsonObject jsonObject = JsonParser.parseString(jsonContent).getAsJsonObject();
      if (jsonObject.has("javaSpecificationVersions")) {
        JsonElement element = jsonObject.get("javaSpecificationVersions");
        javaSpecificationVersions = new HashSet<>();

        if (element.isJsonArray()) {
          // Handle JSON array format: ["11", "17", "21"]
          JsonArray jsonArray = element.getAsJsonArray();
          for (JsonElement arrayElement : jsonArray) {
            javaSpecificationVersions.add(arrayElement.getAsString());
          }
        } else if (element.isJsonPrimitive()) {
          // Handle string format: "11,17,21" or single value "11"
          String stringValue = element.getAsString();
          if (stringValue.contains(",")) {
            javaSpecificationVersions.addAll(Arrays.asList(stringValue.split("\\s*,\\s*")));
          } else {
            javaSpecificationVersions.add(stringValue.trim());
          }
        }
      } else {
        javaSpecificationVersions = null;
      }
    } catch (Exception e) {
      // If JSON parsing fails, set to null and continue with normal deserialization
      javaSpecificationVersions = null;
    }

    return new MuleApplicationModelExtended(serializer.deserialize(jsonContent), javaSpecificationVersions);
  }

  /**
   * Generates a {@link MuleApplicationModel} with default values based on a {@link MuleArtifactContentResolver}.
   *
   * @param originalMuleArtifact original mule application model
   * @param muleArtifactContentResolver the application content resolver
   */
  private MuleApplicationModel generateMuleArtifactWithDefaultValues(MuleApplicationModel originalMuleArtifact,
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
   * @param builder builder for the generated mule artifact based on the original one
   * @param originalMuleArtifact original mule application model
   * @param muleArtifactContentResolver the application content resolver
   */
  private void setBuilderWithDefaultValuesNotPresent(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                     MuleApplicationModel originalMuleArtifact,
                                                     MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    setBuilderWithDefaultName(builder, originalMuleArtifact, muleArtifactContentResolver);
    setBuilderWithDefaultSecureProperties(builder, originalMuleArtifact);
    setBuilderWithDefaultRedeploymentEnabled(builder, originalMuleArtifact);
    setBuilderWithDefaultConfigsValue(builder, originalMuleArtifact, muleArtifactContentResolver);
    setBuilderWithDefaultRequiredProduct(builder, originalMuleArtifact, muleArtifactContentResolver);
    // setBuilderWithDefaultExportedPackagesValue(builder, muleArtifactContentResolver);
    setBuilderWithDefaultExportedResourcesValue(builder, originalMuleArtifact, muleArtifactContentResolver);
    setBuilderWithIncludeTestDependencies(builder, muleArtifactContentResolver);
    setBuilderWithDefaultBundleDescriptorLoaderValue(builder, originalMuleArtifact, muleArtifactContentResolver);

    if (originalMuleArtifact.getLogConfigFile() != null) {
      builder.setLogConfigFile(originalMuleArtifact.getLogConfigFile());
    }
  }

  /**
   * Sets the builder with the default required {@link Product}
   * 
   * @param builder builder for the mule artifact that is going to be generated
   * @param originalMuleArtifact the application content resolver
   * @param muleArtifactContentResolver the application content resolver
   */
  protected abstract void setBuilderWithDefaultRequiredProduct(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                               MuleApplicationModel originalMuleArtifact,
                                                               MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException;

  /**
   * Updates a {@link MuleApplicationModel.MuleApplicationModelBuilder} exportedResources in the attributes field with default
   * values based on a {@link MuleArtifactContentResolver}.
   *
   * @param builder builder for the mule artifact that is going to be generated
   * @param originalMuleArtifact the application content resolver
   * @param muleArtifactContentResolver the application content resolver
   */
  protected abstract void setBuilderWithDefaultExportedResourcesValue(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                                      MuleApplicationModel originalMuleArtifact,
                                                                      MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException;

  /**
   * Updates a {@link MuleApplicationModel.MuleApplicationModelBuilder} bundleDescriptorLoader field with default the default
   * value based on the {@link MuleApplicationModel}.
   *
   * @param builder builder for the mule artifact that is going to be generated
   * @param originalMuleArtifact the application content resolver
   */
  protected abstract void setBuilderWithDefaultBundleDescriptorLoaderValue(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                                           MuleApplicationModel originalMuleArtifact,
                                                                           MuleArtifactContentResolver artifactContentResolver);

  /**
   * Sets the builder with an empty list of secure properties if not set in originalMuleArtifact
   *
   * @param builder builder for the mule artifact that is going to be generated
   * @param originalMuleArtifact the application content resolver
   */
  protected void setBuilderWithDefaultSecureProperties(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                       MuleApplicationModel originalMuleArtifact) {
    List<String> secureProperties = originalMuleArtifact.getSecureProperties();
    if (secureProperties == null) {
      secureProperties = new ArrayList<>();
    }
    builder.setSecureProperties(secureProperties);
  }

  /**
   * Sets the name as groupId:artifactId:version if not set in the originalMuleArtifact
   *
   * @param builder builder for the mule artifact that is going to be generated
   * @param originalMuleArtifact the application content resolver
   * @param muleArtifactContentResolver the application content resolver
   */
  protected void setBuilderWithDefaultName(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                           MuleApplicationModel originalMuleArtifact,
                                           MuleArtifactContentResolver muleArtifactContentResolver) {
    String name = originalMuleArtifact.getName();
    if (isEmpty(name)) {
      Pom pom = muleArtifactContentResolver.getPom();
      name = pom.getGroupId() + ":" + pom.getArtifactId() + ":" + pom.getVersion();
    }
    builder.setName(name);
  }

  protected void setBuilderWithIncludeTestDependencies(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                       MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    MuleArtifactLoaderDescriptor descriptorLoader = builder.getClassLoaderModelDescriptorLoader();
    if (muleArtifactContentResolver.getProjectStructure().getTestConfigsPath().isPresent()) {
      Map<String, Object> attributesCopy = getUpdatedAttributes(descriptorLoader, "includeTestDependencies", "true");
      builder.withClassLoaderModelDescriptorLoader(new MuleArtifactLoaderDescriptor(descriptorLoader.getId(), attributesCopy));
    }
  }

  /**
   * Updates a {@link MuleApplicationModel.MuleApplicationModelBuilder} exportedPackages in the attributes field with default
   * values based on a {@link MuleArtifactContentResolver}.
   *
   * @param builder builder for the mule artifact that is going to be generated
   * @param originalMuleArtifact original mule application model
   * @param muleArtifactContentResolver the application content resolver
   */
  protected abstract void setBuilderWithDefaultExportedPackagesValue(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                                     MuleApplicationModel originalMuleArtifact,
                                                                     MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException;

  /**
   * Updates a {@link MuleApplicationModel.MuleApplicationModelBuilder} configs field with default values based on a
   * {@link MuleArtifactContentResolver}.
   *
   * @param builder builder for the mule artifact that is going to be generated
   * @param originalMuleArtifact original mule application model
   * @param muleArtifactContentResolver the application content resolver
   */
  protected void setBuilderWithDefaultConfigsValue(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                   MuleApplicationModel originalMuleArtifact,
                                                   MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {

    Set<String> configs = getConfigs(originalMuleArtifact, muleArtifactContentResolver);
    configs.addAll(muleArtifactContentResolver.getTestConfigs());
    builder.setConfigs(configs);
  }

  /**
   * Updates a {@link MuleApplicationModel.MuleApplicationModelBuilder} redeploymentEnabled field with default the default value
   * based on the {@link MuleApplicationModel}.
   *
   * @param builder builder for the mule artifact that is going to be generated
   * @param originalMuleArtifact the application content resolver
   */
  protected void setBuilderWithDefaultRedeploymentEnabled(MuleApplicationModel.MuleApplicationModelBuilder builder,
                                                          MuleApplicationModel originalMuleArtifact) {
    builder.setRedeploymentEnabled(originalMuleArtifact.isRedeploymentEnabled());
  }

  /**
   * Builds a {@link MuleApplicationModel.MuleApplicationModelBuilder} with the required values of a {@link MuleApplicationModel}.
   *
   * @param muleArtifact mule application model that is going to be used to generate the builder
   */
  protected MuleApplicationModel.MuleApplicationModelBuilder getBuilderWithRequiredValues(MuleApplicationModel muleArtifact) {
    MuleApplicationModel.MuleApplicationModelBuilder builder = new MuleApplicationModel.MuleApplicationModelBuilder();
    builder.setMinMuleVersion(muleArtifact.getMinMuleVersion());
    return builder;
  }

  protected Set<String> getConfigs(MuleApplicationModel originalMuleArtifact,
                                   MuleArtifactContentResolver muleArtifactContentResolver)
      throws IOException {
    Set<String> configs = new HashSet<>();
    if (originalMuleArtifact.getConfigs() != null) {
      configs.addAll(originalMuleArtifact.getConfigs());
    } else {
      configs.addAll(muleArtifactContentResolver.getConfigs());
    }

    return configs;
  }

  protected Map<String, Object> getUpdatedAttributes(MuleArtifactLoaderDescriptor descriptorLoader, String attribute,
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
   * Writes the extended mule artifact (including javaSpecificationVersions) to the corresponding file
   *
   * @param extendedMuleArtifact extended mule artifact that contains both the model and javaSpecificationVersions
   * @param destinationFolder destination folder where the mule-artifact.json file is going to be created
   */
  public void writeMuleArtifactExtendedToFile(MuleApplicationModelExtended extendedMuleArtifact, Path destinationFolder)
      throws IOException {
    // Serialize the original MuleApplicationModel to JSON string
    String originalJsonContent = serializer.serialize(extendedMuleArtifact.getModel());

    // Parse the JSON to add the javaSpecificationVersions property
    String finalJsonContent;
    try {
      JsonObject jsonObject = JsonParser.parseString(originalJsonContent).getAsJsonObject();

      // Add javaSpecificationVersions property if it exists
      if (extendedMuleArtifact.getJavaSpecificationVersions() != null &&
          !extendedMuleArtifact.getJavaSpecificationVersions().isEmpty()) {

        // Convert Set<String> to JsonArray
        JsonArray jsonArray = new JsonArray();
        for (String version : extendedMuleArtifact.getJavaSpecificationVersions()) {
          jsonArray.add(version);
        }
        jsonObject.add("javaSpecificationVersions", jsonArray);
      }

      // Convert back to pretty-printed JSON string
      Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
      finalJsonContent = gson.toJson(jsonObject);
    } catch (Exception e) {
      // If JSON manipulation fails, fall back to original content
      finalJsonContent = originalJsonContent;
    }

    // Write to file
    File generatedMuleArtifactJson = new File(destinationFolder.toFile(), MULE_ARTIFACT_JSON);
    FileUtils.writeStringToFile(generatedMuleArtifactJson, finalJsonContent, (String) null);
  }
}
