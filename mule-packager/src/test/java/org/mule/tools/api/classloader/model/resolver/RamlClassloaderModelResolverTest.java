/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader.model.resolver;

import org.junit.Before;
import org.junit.Test;
import org.mule.maven.client.api.model.BundleDependency;
import org.mule.maven.client.api.model.BundleDescriptor;
import org.mule.maven.client.internal.AetherMavenClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RamlClassloaderModelResolverTest {

  private RamlClassloaderModelResolver resolver;
  private List<BundleDependency> appDependencies;

  @Before
  public void setUp() {
    appDependencies = buildDependencies(5);
    resolver = new RamlClassloaderModelResolver(mock(AetherMavenClient.class));
  }


  @Test(expected = IllegalArgumentException.class)
  public void resolveRamlDependenciesNull() {
    resolver.resolveRamlDependencies(null);
  }

  @Test
  public void resolveRamlDependencies() {
    RamlClassloaderModelResolver resolverSpy = spy(resolver);
    doNothing().when(resolverSpy).collectDependencies(any());
    int numberDependenciesToBeResolved = 3;
    List<BundleDependency> ramls = buildDependencies(numberDependenciesToBeResolved);
    resolverSpy.resolveDependencies(ramls);
    verify(resolverSpy, times(numberDependenciesToBeResolved)).collectDependencies(any());
  }

  @Test
  public void resolveRamlDependenciesDoNotVisitAgain() {
    RamlClassloaderModelResolver resolverSpy = spy(resolver);
    doNothing().when(resolverSpy).collectDependencies(any());
    int numberDependenciesToBeResolved = 3;
    List<BundleDependency> ramls = buildDependencies(numberDependenciesToBeResolved);
    resolverSpy.visited = new HashSet<>();
    resolverSpy.visited.add(ramls.get(0).getDescriptor());
    resolverSpy.resolveRamlDependencies(ramls);
    verify(resolverSpy, times(numberDependenciesToBeResolved - 1)).collectDependencies(any());
  }

  @Test
  public void collectDependencies() {
    BundleDependency ramlMock = mock(BundleDependency.class);
    RamlClassloaderModelResolver resolverSpy = spy(resolver);
    doNothing().when(resolverSpy).markVisited(any());
    resolverSpy.muleDependenciesDependencies = new HashMap<>();
    doReturn(false).when(resolverSpy).shouldVisit(any());
    doReturn(newArrayList(mock(BundleDependency.class))).when(resolverSpy).getDependencies(any());
    doReturn(false).when(resolverSpy).alreadyVisited(any());

    resolverSpy.collectDependencies(ramlMock);

    verify(resolverSpy).alreadyVisited(any());
    verify(resolverSpy).getDependencies(any());
    verify(resolverSpy).shouldVisit(any());
    verify(resolverSpy).markVisited(any());
  }

  private List<BundleDependency> buildDependencies(int i) {
    List<BundleDependency> dependencies = new ArrayList<>(i);
    while (i-- > 0) {
      BundleDependency dependency = mock(BundleDependency.class);
      when(dependency.getDescriptor()).thenReturn(mock(BundleDescriptor.class));
      dependencies.add(dependency);
    }
    return dependencies;
  }

}
