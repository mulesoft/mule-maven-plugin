/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader;



/**
 * The following test is to validate compatibility between new version of the ClassLoaderModel.
 * This is needed because, if we intend to package applications with added fields in the ClassLoaderModel,
 * they should behave the same way even deployed in runtime versions that use a previous version of the mule-maven-plugin.
 */
public class ClassLoaderModelSerializationCompatibilityTestCase {
  /*
  private static final String GROUP_ID = "group.id";
  private static final String APP_ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  
  private static final String PLUGIN_ARTIFACT_ID = "plugin-id";
  
  private static final URI TEST_URI = URI.create("test.com");
  
  private static final ArtifactCoordinates appArtifactCoordinates = new ArtifactCoordinates(GROUP_ID, APP_ARTIFACT_ID, VERSION);
  private static final ArtifactCoordinates pluginArtifactCoordinates =
      new ArtifactCoordinates(GROUP_ID, PLUGIN_ARTIFACT_ID, VERSION);
  
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  
  @Test
  public void testSerializationOfNewArtifactVersion() throws Exception {
    List<Artifact> dependencies = new ArrayList<>();
    dependencies.add(new ArtifactWithExtraFields(pluginArtifactCoordinates, TEST_URI, "transientField"));
    ClassLoaderModel classLoaderModel = new ClassLoaderModel(VERSION, appArtifactCoordinates);
    classLoaderModel.setDependencies(dependencies);
    File serializedClassLoaderModel = serializeToFile(classLoaderModel, temporaryFolder.newFolder());
    ClassLoaderModel deserializedClassLoaderModel = deserialize(serializedClassLoaderModel);
    assertModelsAreEqual(classLoaderModel, deserializedClassLoaderModel);
  }
  
  @Test
  public void testSerializationOfNewModelVersion() throws Exception {
    List<Artifact> dependencies = new ArrayList<>();
    dependencies.add(new Artifact(pluginArtifactCoordinates, TEST_URI));
    ClassLoaderModel classLoaderModel = new ClassLoaderModelWithExtraField(VERSION, appArtifactCoordinates, "transientField");
    classLoaderModel.setDependencies(dependencies);
    File serializedClassLoaderModel = serializeToFile(classLoaderModel, temporaryFolder.newFolder());
    ClassLoaderModel deserializedClassLoaderModel = deserialize(serializedClassLoaderModel);
    assertModelsAreEqual(classLoaderModel, deserializedClassLoaderModel);
  }
  
  @Test
  public void testSerialzationOfNewModelAndArtifactVersion() throws Exception {
    List<Artifact> dependencies = new ArrayList<>();
    dependencies.add(new ArtifactWithExtraFields(pluginArtifactCoordinates, TEST_URI, "transientField"));
    ClassLoaderModel classLoaderModel = new ClassLoaderModelWithExtraField(VERSION, appArtifactCoordinates, "transientField");
    classLoaderModel.setDependencies(dependencies);
    File serializedClassLoaderModel = serializeToFile(classLoaderModel, temporaryFolder.newFolder());
    ClassLoaderModel deserializedClassLoaderModel = deserialize(serializedClassLoaderModel);
    assertModelsAreEqual(classLoaderModel, deserializedClassLoaderModel);
  }
  
  private void assertModelsAreEqual(ClassLoaderModel originalModel, ClassLoaderModel deserializedModel) {
    assertThat(originalModel, equalTo(deserializedModel));
    List<Artifact> originalDependencies = originalModel.getDependencies();
    List<Artifact> deserializedDependencies = deserializedModel.getDependencies();
    for (int i = 0; i < originalDependencies.size(); i++) {
      Artifact originalArtifact = originalDependencies.get(i);
      Artifact deserializedArtifact = deserializedDependencies.get(i);
      assertThat(originalArtifact, equalTo(deserializedArtifact));
    }
  
  }
  
  private class ArtifactWithExtraFields extends Artifact {
  
    private String newArtifactField;
  
    public ArtifactWithExtraFields(ArtifactCoordinates artifactCoordinates, URI uri, String extraField) {
      super(artifactCoordinates, uri);
      this.newArtifactField = extraField;
    }
  
    public String getNewArtifactField() {
      return newArtifactField;
    }
  
  }
  
  private class ClassLoaderModelWithExtraField extends ClassLoaderModel {
  
    private String newField;
  
    public ClassLoaderModelWithExtraField(String version, ArtifactCoordinates artifactCoordinates, String extraField) {
      super(version, artifactCoordinates);
      this.newField = extraField;
    }
  
    public String getNewField() {
      return this.newField;
    }
  
  }
  */

}
