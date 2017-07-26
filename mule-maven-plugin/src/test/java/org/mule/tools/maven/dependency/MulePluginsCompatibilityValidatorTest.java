/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.dependency;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

public class MulePluginsCompatibilityValidatorTest {

  private MulePluginsCompatibilityValidator validator;
  private List<Dependency> dependencies;
  private List<Dependency> dependencies1;
  private List<Dependency> dependencies2;
  private final Dependency DEPENDENCY0 = createDependency(0, "1.0.0", "mule-plugin");
  private final Dependency DEPENDENCY1 = createDependency(1, "1.0.1", "mule-plugin");
  private final Dependency DEPENDENCY2 = createDependency(2, "1.0.2", "mule-plugin");

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void before() {
    validator = new MulePluginsCompatibilityValidator();
    dependencies = new ArrayList<>();
    dependencies1 = new ArrayList<>();
    dependencies2 = new ArrayList<>();
  }

  @Test
  public void validateEmptyListTest() {
    try {
      validator.validate(Collections.emptyList());
    } catch (MojoExecutionException e) {
      fail("Empty list should be valid");
    }
  }

  @Test
  public void validateUnitListTest() {
    dependencies.add(DEPENDENCY0);
    try {
      validator.validate(dependencies);
    } catch (MojoExecutionException e) {
      fail("Unit list should be valid");
    }
  }

  @Test
  public void validateDifferentDependenciesListTest() {
    dependencies.add(createDependency(0, "0.8.0", "mule-plugin"));
    dependencies.add(createDependency(1, "1.0.0", "mule-plugin"));
    try {
      validator.validate(dependencies);
    } catch (MojoExecutionException e) {
      fail("Different mule-plugins should be valid");
    }
  }

  @Test
  public void validateSameDependenciesNotCompatibleVersionsListTest() throws MojoExecutionException {
    expectedException.expect(MojoExecutionException.class);
    dependencies.add(createDependency(0, "0.8.0", "mule-plugin"));
    dependencies.add(createDependency(0, "1.0.0", "mule-plugin"));
    validator.validate(dependencies);
  }

  // Reflexive
  @Test
  public void validateSameMulePluginTest() {
    dependencies.add(DEPENDENCY0);
    dependencies.add(DEPENDENCY0);

    assertThat("A mule plugin should be self-compatible", validator.areMulePluginVersionCompatible(dependencies),
               is(true));
  }

  // Symmetric
  @Test
  public void validateOrderMulePluginTest() {
    dependencies1 = new ArrayList<>();

    dependencies1.add(DEPENDENCY0);
    dependencies1.add(DEPENDENCY1);

    dependencies2 = new ArrayList<>();

    dependencies2.add(DEPENDENCY1);
    dependencies2.add(DEPENDENCY0);

    assertThat("Validation should order-independent",
               validator.areMulePluginVersionCompatible(dependencies1) == validator.areMulePluginVersionCompatible(dependencies2),
               is(true));
  }

  // Transitive
  @Test
  public void validateDifferentMulePluginsTest() {
    dependencies.add(DEPENDENCY0);
    dependencies.add(DEPENDENCY1);

    dependencies1.add(DEPENDENCY1);
    dependencies1.add(DEPENDENCY2);

    dependencies2.add(DEPENDENCY0);
    dependencies2.add(DEPENDENCY2);

    assertThat("Validation should be valid for all elements of same version",
               validator.areMulePluginVersionCompatible(dependencies) && validator
                   .areMulePluginVersionCompatible(dependencies1) == validator.areMulePluginVersionCompatible(dependencies2),
               is(true));
  }

  @Test
  public void areMulePluginVersionNotCompatibleTest() {
    dependencies.add(createDependency(0, "0.8.0", "mule-plugin"));
    dependencies.add(createDependency(1, "1.0.1", "mule-plugin"));

    assertThat("Mule plugins should be considered version compatible", validator.areMulePluginVersionCompatible(dependencies),
               is(false));
  }

  @Test
  public void areMulePluginVersionNotCompatibleAtLeastOneNotCompatibleTest() {
    dependencies.add(createDependency(0, "0.8.0", "mule-plugin"));
    dependencies.add(createDependency(1, "1.0.1", "mule-plugin"));
    dependencies.add(createDependency(2, "0.8.1", "mule-plugin"));

    assertThat("Mule plugins should be considered version compatible", validator.areMulePluginVersionCompatible(dependencies),
               is(false));
  }

  @Test
  public void areMulePluginVersionNotCompatibleWithAllNotCompatibleTest() {
    dependencies.add(createDependency(0, "0.8.0", "mule-plugin"));
    dependencies.add(createDependency(1, "1.0.1", "mule-plugin"));
    dependencies.add(createDependency(2, "2.0.1", "mule-plugin"));

    assertThat("Mule plugins should be considered version compatible", validator.areMulePluginVersionCompatible(dependencies),
               is(false));
  }

  @Test
  public void areMulePluginVersionCompatibleEmptyListTest() {
    assertThat("Mule plugins empty list should be considered version compatible",
               validator.areMulePluginVersionCompatible(dependencies),
               is(true));
  }

  @Test
  public void areMulePluginVersionCompatibleOneElementTest() {
    dependencies.add(createDependency(0, "1.0.0", "mule-plugin"));

    assertThat("Mule plugins unit list should be considered version compatible",
               validator.areMulePluginVersionCompatible(dependencies),
               is(true));
  }

  @Test
  public void areMulePluginVersionCompatibleMultipleElementsTest() {
    dependencies.add(createDependency(0, "1.0.0", "mule-plugin"));
    dependencies.add(createDependency(1, "1.0.1", "mule-plugin"));
    dependencies.add(createDependency(2, "1.1.0", "mule-plugin"));
    dependencies.add(createDependency(3, "1.1.1", "mule-plugin"));

    assertThat("Mule plugins should be considered version compatible", validator.areMulePluginVersionCompatible(dependencies),
               is(true));
  }

  @Test
  public void buildEmptyDependencyMapTest() {
    List<Dependency> dependencies = new ArrayList<>();
    Map<String, List<Dependency>> actualDependencyMap = validator.buildDependencyMap(dependencies);
    assertThat("Dependency map should be empty", actualDependencyMap.size(), equalTo(0));
  }

  @Test
  public void buildDependencyMapMulePluginDifferentVersionsTest() {
    List<Dependency> dependencies = new ArrayList<>();

    dependencies.add(createDependency(0, "1.0.0", "mule-plugin"));
    dependencies.add(createDependency(0, "1.0.1", "mule-plugin"));
    dependencies.add(createDependency(0, "1.1.0", "mule-plugin"));
    dependencies.add(createDependency(0, "1.1.1", "mule-plugin"));

    Map<String, List<Dependency>> actualDependencyMap = validator.buildDependencyMap(dependencies);
    assertThat("Dependency map should contain 1 element", actualDependencyMap.size(), equalTo(1));
    assertThat("The dependency list should contain 4 elements",
               actualDependencyMap.values().stream().allMatch(l -> l.size() == 4), is(true));
  }

  @Test
  public void buildDependencyMapUniquePluginsTest() {
    List<Dependency> dependencies = new ArrayList<>();

    dependencies.add(createDependency(0, "1.0.0", "mule-plugin"));
    dependencies.add(createDependency(1, "1.0.0", "mule-plugin"));
    dependencies.add(createDependency(2, "1.0.0", "mule-plugin"));

    Map<String, List<Dependency>> actualDependencyMap = validator.buildDependencyMap(dependencies);
    assertThat("Dependency map should contain 3 elements", actualDependencyMap.size(), equalTo(3));
    assertThat("Every dependency list should contain 1 element",
               actualDependencyMap.values().stream().allMatch(l -> l.size() == 1), is(true));
  }

  private List<Dependency> getDependencies(int i) {
    if (i <= 0) {
      return new ArrayList<>();
    }
    List<Dependency> dependencies = getDependencies(i - 1);
    dependencies.add(createDependency(i, "1.0.0", ""));
    return dependencies;
  }

  private Dependency createDependency(int i, String version, String classifier) {
    Dependency dependency = new Dependency();
    dependency.setGroupId("group.id." + i);
    dependency.setArtifactId("artifact-id" + i);
    dependency.setVersion(version);
    dependency.setType("jar");
    dependency.setClassifier(classifier);
    return dependency;
  }

}
