/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.packager.filter;

import org.junit.Before;
import org.junit.Test;
import org.mule.tools.api.packager.filter.predicate.ArtifactPredicate;
import org.mule.tools.api.util.Artifact;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArtifactFilterTest {

  private ArtifactFilter artifactFilter;
  private ArtifactPredicate predicateMock;
  private Set<Artifact> setArtifacts;

  @Before
  public void setUp() {
    artifactFilter = new ArtifactFilter();
    predicateMock = mock(ArtifactPredicate.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void fromArtifactsNullTest() {
    artifactFilter.filter(null, new HashSet<Artifact>(), predicateMock);
  }

  @Test(expected = IllegalArgumentException.class)
  public void toArtifactsNullTest() {
    artifactFilter.filter(new HashSet<Artifact>(), null, predicateMock);
  }

  @Test(expected = IllegalArgumentException.class)
  public void predicateNullTest() {
    artifactFilter.filter(new HashSet<Artifact>(), new HashSet<Artifact>(), null);
  }

  @Test
  public void fromArtifactsEmptyTest() {
    Set<Artifact> toArtifacts = getSetOfArtifacts();
    setUpPredicate(predicateMock, toArtifacts, true);

    Set<Artifact> filteredArtifacts = artifactFilter.filter(new HashSet<Artifact>(), toArtifacts, predicateMock);

    assertThat("Sets should be equal", filteredArtifacts.equals(toArtifacts));
  }

  @Test
  public void toArtifactsEmptyTest() {
    Set<Artifact> fromArtifacts = getSetOfArtifacts();
    setUpPredicate(predicateMock, fromArtifacts, true);

    Set<Artifact> filteredArtifacts = artifactFilter.filter(fromArtifacts, new HashSet<Artifact>(), predicateMock);

    assertThat("Sets should be equal", filteredArtifacts.equals(fromArtifacts));
  }

  @Test
  public void filterAllArtifactsTest() {
    Set<Artifact> fromArtifacts = getSetOfArtifacts();
    Set<Artifact> toArtifacts = getSetOfArtifacts();
    setUpPredicate(predicateMock, fromArtifacts, false);
    setUpPredicate(predicateMock, fromArtifacts, true);

    Set<Artifact> filteredArtifacts = artifactFilter.filter(fromArtifacts, toArtifacts, predicateMock);

    assertThat("Sets should be equal", filteredArtifacts.equals(toArtifacts));
  }

  @Test
  public void doNotFilterAnyArtifactTest() {
    Set<Artifact> fromArtifacts = getSetOfArtifacts();
    Set<Artifact> toArtifacts = getSetOfArtifacts();
    setUpPredicate(predicateMock, fromArtifacts, true);
    setUpPredicate(predicateMock, fromArtifacts, true);

    Set<Artifact> filteredArtifacts = artifactFilter.filter(fromArtifacts, toArtifacts, predicateMock);

    Set<Artifact> expectedResult = new HashSet<>(fromArtifacts);
    expectedResult.addAll(toArtifacts);

    assertThat("Sets should be equal", filteredArtifacts.equals(expectedResult));
  }

  private void setUpPredicate(ArtifactPredicate predicateMock, Set<Artifact> artifacts, boolean predicateResult) {
    for (Artifact artifact : artifacts) {
      when(predicateMock.test(artifact)).thenReturn(predicateResult);
    }
  }

  public Set<Artifact> getSetOfArtifacts() {
    return newHashSet(mock(Artifact.class), mock(Artifact.class), mock(Artifact.class));
  }
}
