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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ApplicationClassloaderModelTest {

  public static final String VERSION = "1.0.0";
  @Mock
  private DefaultClassLoaderModel classloaderModelMock;
  private List<DefaultClassLoaderModel> mulePluginClassloaderModels;
  private List<DefaultClassLoaderModel> appDependenciesClassloaderModels;

  @BeforeEach
  public void setUp() {
    initMocks(this);
    mulePluginClassloaderModels = buildMulePluginClassloaderModelListMock();
    appDependenciesClassloaderModels = new ArrayList<>(mulePluginClassloaderModels.subList(0, 2));
    appDependenciesClassloaderModels.addAll(buildRamlClassloaderModelListMock());

    when(classloaderModelMock.getArtifacts())
        .thenReturn(appDependenciesClassloaderModels.stream()
            .map(DefaultClassLoaderModel::getArtifacts)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet()));
  }

  @DisplayName("Get artifacts method")
  @Test
  public void getArtifacts() {
    ApplicationClassloaderModel appClassloaderModel = new ApplicationClassloaderModel(classloaderModelMock)
        .addAllMulePluginClassloaderModels(mulePluginClassloaderModels);

    Set<Artifact> expectedArtifacts = Stream.concat(
                                                    appDependenciesClassloaderModels.stream()
                                                        .map(DefaultClassLoaderModel::getArtifacts).flatMap(Set::stream),
                                                    mulePluginClassloaderModels.stream()
                                                        .map(DefaultClassLoaderModel::getArtifacts).flatMap(Set::stream))
        .collect(Collectors.toSet());

    assertThat("Should be the same set", appClassloaderModel.getArtifacts(), equalTo(expectedArtifacts));
  }

  private List<DefaultClassLoaderModel> buildMulePluginClassloaderModelListMock() {
    return IntStream.rangeClosed(1, 4)
        .mapToObj(this::buildMulePluginClassloaderModel)
        .collect(Collectors.toList());
  }

  private DefaultClassLoaderModel buildMulePluginClassloaderModel(int i) {
    return new DefaultClassLoaderModel(VERSION, buildMulePluginArtifactCoordinates(i, "1.0.0"))
        .setDependencies(buildMulePluginArtifacts(i));
  }

  private List<Artifact> buildMulePluginArtifacts(int i) {
    int prefix = i * 10;
    return IntStream.rangeClosed(prefix + 1, prefix + 5)
        .mapToObj(index -> new Artifact(buildMulePluginArtifactCoordinates(index, "1.0.0"), URI.create("fake" + index)))
        .collect(Collectors.toList());
  }

  private ArtifactCoordinates buildMulePluginArtifactCoordinates(int n, String version) {
    return new ArtifactCoordinates("org.mule.connectors", "connector-" + n, version, "jar", "mule-plugin");
  }

  private List<DefaultClassLoaderModel> buildRamlClassloaderModelListMock() {
    return IntStream.rangeClosed(1, 4)
        .mapToObj(this::buildRamlClassloaderModel)
        .collect(Collectors.toList());
  }

  private DefaultClassLoaderModel buildRamlClassloaderModel(int i) {
    return new DefaultClassLoaderModel(VERSION, buildRamlArtifactCoordinates(i, "1.0.0", ""))
        .setDependencies(buildRamlArtifacts(i));
  }

  private List<Artifact> buildRamlArtifacts(int i) {
    int prefix = i * 10;
    return IntStream.rangeClosed(prefix + 1, prefix + 5)
        .mapToObj(index -> new Artifact(buildRamlArtifactCoordinates(index, "1.0.0", "-fragment"), URI.create("fake" + index)))
        .collect(Collectors.toList());
  }

  private ArtifactCoordinates buildRamlArtifactCoordinates(int n, String version, String packagingSuffix) {
    return new ArtifactCoordinates("org.mycompany", "raml-" + n, version, "zip", "raml" + packagingSuffix);
  }
}
