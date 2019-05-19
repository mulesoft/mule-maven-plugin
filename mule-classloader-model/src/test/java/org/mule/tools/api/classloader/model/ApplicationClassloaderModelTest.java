/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader.model;

import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplicationClassloaderModelTest {

  public static final String VERSION = "1.0.0";
  private ClassLoaderModel classloaderModelMock;
  private ApplicationClassloaderModel appClassloaderModel;
  private List<ClassLoaderModel> mulePluginClassloaderModels;
  private List<ClassLoaderModel> appDependenciesClassloaderModels;
  private List<ClassLoaderModel> ramlClassloaderModels;

  @Before
  public void setUp() {
    mulePluginClassloaderModels = buildMulePluginClassloaderModelListMock();
    ramlClassloaderModels = buildRamlClassloaderModelListMock();
    appDependenciesClassloaderModels = new ArrayList<>(mulePluginClassloaderModels.subList(0, 2));
    appDependenciesClassloaderModels.addAll(ramlClassloaderModels);
    classloaderModelMock = mock(ClassLoaderModel.class);

    when(classloaderModelMock.getArtifacts())
        .thenReturn(appDependenciesClassloaderModels.stream()
            .map(ClassLoaderModel::getArtifacts)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet()));

    appClassloaderModel = new ApplicationClassloaderModel(classloaderModelMock);
    appClassloaderModel.addAllMulePluginClassloaderModels(mulePluginClassloaderModels);
  }

  @Test
  public void getArtifacts() {
    Set<Artifact> expectedArtifacts = new HashSet<>();

    for (ClassLoaderModel cl : appDependenciesClassloaderModels) {
      expectedArtifacts.addAll(cl.getArtifacts());
    }
    for (ClassLoaderModel cl : mulePluginClassloaderModels) {
      expectedArtifacts.addAll(cl.getArtifacts());
    }

    assertThat("Should be the same set", appClassloaderModel.getArtifacts(), equalTo(expectedArtifacts));
  }

  private List<ClassLoaderModel> buildMulePluginClassloaderModelListMock() {
    ClassLoaderModel cl1 = buildMulePluginClassloaderModel(1);
    ClassLoaderModel cl2 = buildMulePluginClassloaderModel(2);
    ClassLoaderModel cl3 = buildMulePluginClassloaderModel(3);
    ClassLoaderModel cl4 = buildMulePluginClassloaderModel(4);
    return newArrayList(cl1, cl2, cl3, cl4);
  }

  private ClassLoaderModel buildMulePluginClassloaderModel(int i) {
    ClassLoaderModel cl = new ClassLoaderModel(VERSION, buildMulePluginArtifactCoordinates(i, "1.0.0"));
    cl.setDependencies(buildMulePluginArtifacts(i));
    return cl;
  }

  private List<Artifact> buildMulePluginArtifacts(int i) {
    int prefix = i * 10;
    Artifact a1 = new Artifact(buildMulePluginArtifactCoordinates(prefix + 1, "1.0.0"), URI.create("fake" + (prefix + 1)));
    Artifact a2 = new Artifact(buildMulePluginArtifactCoordinates(prefix + 2, "1.0.0"), URI.create("fake" + (prefix + 2)));
    Artifact a3 = new Artifact(buildMulePluginArtifactCoordinates(prefix + 3, "1.0.0"), URI.create("fake" + (prefix + 3)));
    Artifact a4 = new Artifact(buildMulePluginArtifactCoordinates(prefix + 4, "1.0.0"), URI.create("fake" + (prefix + 4)));
    Artifact a5 = new Artifact(buildMulePluginArtifactCoordinates(prefix + 5, "1.0.0"), URI.create("fake" + (prefix + 5)));
    return newArrayList(a1, a2, a3, a4, a5);

  }

  private ArtifactCoordinates buildMulePluginArtifactCoordinates(int n, String version) {
    return new ArtifactCoordinates("org.mule.connectors", "connector-" + n, version, "jar", "mule-plugin");
  }


  private List<ClassLoaderModel> buildRamlClassloaderModelListMock() {
    ClassLoaderModel cl1 = buildRamlClassloaderModel(1);
    ClassLoaderModel cl2 = buildRamlClassloaderModel(2);
    ClassLoaderModel cl3 = buildRamlClassloaderModel(3);
    ClassLoaderModel cl4 = buildRamlClassloaderModel(4);
    return newArrayList(cl1, cl2, cl3, cl4);
  }

  private ClassLoaderModel buildRamlClassloaderModel(int i) {
    ClassLoaderModel cl = new ClassLoaderModel(VERSION, buildRamlArtifactCoordinates(i, "1.0.0", ""));
    cl.setDependencies(buildRamlArtifacts(i));
    return cl;
  }

  private List<Artifact> buildRamlArtifacts(int i) {
    int prefix = i * 10;
    Artifact a1 = new Artifact(buildRamlArtifactCoordinates(prefix + 1, "1.0.0", "-fragment"), URI.create("fake" + (prefix + 1)));
    Artifact a2 = new Artifact(buildRamlArtifactCoordinates(prefix + 2, "1.0.0", "-fragment"), URI.create("fake" + (prefix + 2)));
    Artifact a3 = new Artifact(buildRamlArtifactCoordinates(prefix + 3, "1.0.0", "-fragment"), URI.create("fake" + (prefix + 3)));
    Artifact a4 = new Artifact(buildRamlArtifactCoordinates(prefix + 4, "1.0.0", "-fragment"), URI.create("fake" + (prefix + 4)));
    Artifact a5 = new Artifact(buildRamlArtifactCoordinates(prefix + 5, "1.0.0", "-fragment"), URI.create("fake" + (prefix + 5)));
    return newArrayList(a1, a2, a3, a4, a5);

  }

  private ArtifactCoordinates buildRamlArtifactCoordinates(int n, String version, String packagingSuffix) {
    return new ArtifactCoordinates("org.mycompany", "raml-" + n, version, "zip", "raml" + packagingSuffix);
  }

}
