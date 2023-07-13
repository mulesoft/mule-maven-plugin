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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.api.exception.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class MulePluginsCompatibilityValidatorTest {

  private MulePluginsCompatibilityValidator validator;
  private List<ArtifactCoordinates> dependencies;
  private List<ArtifactCoordinates> dependencies1;
  private List<ArtifactCoordinates> dependencies2;
  private final ArtifactCoordinates DEPENDENCY0 = createDependency(0, "1.0.0", "mule-plugin");
  private final ArtifactCoordinates DEPENDENCY1 = createDependency(1, "1.0.1", "mule-plugin");
  private final ArtifactCoordinates DEPENDENCY2 = createDependency(2, "1.0.2", "mule-plugin");

  @BeforeEach
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
    } catch (ValidationException e) {
      fail("Empty list should be valid");
    }
  }

  @Test
  public void validateUnitListTest() {
    dependencies.add(DEPENDENCY0);
    try {
      validator.validate(dependencies);
    } catch (ValidationException e) {
      fail("Unit list should be valid");
    }
  }

  @Test
  public void validateDifferentDependenciesListTest() {
    dependencies.add(createDependency(0, "0.8.0", "mule-plugin"));
    dependencies.add(createDependency(1, "1.0.0", "mule-plugin"));
    try {
      validator.validate(dependencies);
    } catch (ValidationException e) {
      fail("Different mule-plugins should be valid");
    }
  }

  @Test
  public void validateSameDependenciesNotCompatibleVersionsListTest() {
    assertThrows(ValidationException.class, () -> {
      dependencies.add(createDependency(0, "0.8.0", "mule-plugin"));
      dependencies.add(createDependency(0, "1.0.0", "mule-plugin"));
      validator.validate(dependencies);
    });
  }

  // Reflexive
  @Test
  public void validateSameMulePluginTest() {
    dependencies.add(DEPENDENCY0);
    dependencies.add(DEPENDENCY0);

    assertThat(validator.areMulePluginVersionCompatible(dependencies)).describedAs("A mule plugin should be self-compatible")
        .isTrue();
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

    assertThat(validator.areMulePluginVersionCompatible(dependencies1) == validator.areMulePluginVersionCompatible(dependencies2))
        .describedAs("Validation should order-independent").isTrue();
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

    assertThat(validator.areMulePluginVersionCompatible(dependencies) && validator
        .areMulePluginVersionCompatible(dependencies1) == validator.areMulePluginVersionCompatible(dependencies2))
            .describedAs("Validation should be valid for all elements of same version").isTrue();
  }

  @Test
  public void areMulePluginVersionNotCompatibleTest() {
    dependencies.add(createDependency(0, "0.8.0", "mule-plugin"));
    dependencies.add(createDependency(1, "1.0.1", "mule-plugin"));

    assertThat(validator.areMulePluginVersionCompatible(dependencies))
        .describedAs("Mule plugins should be considered version compatible").isFalse();
  }

  @Test
  public void areMulePluginVersionNotCompatibleAtLeastOneNotCompatibleTest() {
    dependencies.add(createDependency(0, "0.8.0", "mule-plugin"));
    dependencies.add(createDependency(1, "1.0.1", "mule-plugin"));
    dependencies.add(createDependency(2, "0.8.1", "mule-plugin"));

    assertThat(validator.areMulePluginVersionCompatible(dependencies))
        .describedAs("Mule plugins should be considered version compatible").isFalse();
  }

  @Test
  public void areMulePluginVersionNotCompatibleWithAllNotCompatibleTest() {
    dependencies.add(createDependency(0, "0.8.0", "mule-plugin"));
    dependencies.add(createDependency(1, "1.0.1", "mule-plugin"));
    dependencies.add(createDependency(2, "2.0.1", "mule-plugin"));

    assertThat(validator.areMulePluginVersionCompatible(dependencies))
        .describedAs("Mule plugins should be considered version compatible").isFalse();
  }

  @Test
  public void areMulePluginVersionCompatibleEmptyListTest() {
    assertThat(validator.areMulePluginVersionCompatible(dependencies))
        .describedAs("Mule plugins empty list should be considered version compatible").isTrue();
  }

  @Test
  public void areMulePluginVersionCompatibleOneElementTest() {
    dependencies.add(createDependency(0, "1.0.0", "mule-plugin"));

    assertThat(validator.areMulePluginVersionCompatible(dependencies))
        .describedAs("Mule plugins unit list should be considered version compatible").isTrue();
  }

  @Test
  public void areMulePluginVersionCompatibleMultipleElementsTest() {
    dependencies.add(createDependency(0, "1.0.0", "mule-plugin"));
    dependencies.add(createDependency(1, "1.0.1", "mule-plugin"));
    dependencies.add(createDependency(2, "1.1.0", "mule-plugin"));
    dependencies.add(createDependency(3, "1.1.1", "mule-plugin"));

    assertThat(validator.areMulePluginVersionCompatible(dependencies))
        .describedAs("Mule plugins should be considered version compatible").isTrue();
  }

  @Test
  public void buildEmptyDependencyMapTest() {
    List<ArtifactCoordinates> dependencies = new ArrayList<>();
    Map<String, List<ArtifactCoordinates>> actualDependencyMap = validator.buildDependencyMap(dependencies);
    assertThat(actualDependencyMap.size()).describedAs("Dependency map should be empty").isEqualTo(0);
  }

  @Test
  public void buildDependencyMapMulePluginDifferentVersionsTest() {
    List<ArtifactCoordinates> dependencies = new ArrayList<>();

    dependencies.add(createDependency(0, "1.0.0", "mule-plugin"));
    dependencies.add(createDependency(0, "1.0.1", "mule-plugin"));
    dependencies.add(createDependency(0, "1.1.0", "mule-plugin"));
    dependencies.add(createDependency(0, "1.1.1", "mule-plugin"));

    Map<String, List<ArtifactCoordinates>> actualDependencyMap = validator.buildDependencyMap(dependencies);
    assertThat(actualDependencyMap.size()).describedAs("Dependency map should contain 1 element").isEqualTo(1);
    assertThat(actualDependencyMap.values().stream().allMatch(l -> l.size() == 4))
        .describedAs("The dependency list should contain 4 elements").isTrue();
  }

  @Test
  public void buildDependencyMapUniquePluginsTest() {
    List<ArtifactCoordinates> dependencies = new ArrayList<>();

    dependencies.add(createDependency(0, "1.0.0", "mule-plugin"));
    dependencies.add(createDependency(1, "1.0.0", "mule-plugin"));
    dependencies.add(createDependency(2, "1.0.0", "mule-plugin"));

    Map<String, List<ArtifactCoordinates>> actualDependencyMap = validator.buildDependencyMap(dependencies);
    assertThat(actualDependencyMap.size()).describedAs("Dependency map should contain 3 elements").isEqualTo(3);
    assertThat(actualDependencyMap.values().stream().allMatch(l -> l.size() == 1))
        .describedAs("Every dependency list should contain 1 element").isTrue();
  }

  private ArtifactCoordinates createDependency(int i, String version, String classifier) {
    return new ArtifactCoordinates("group.id." + i, "artifact-id" + i, version, "jar", classifier);
  }

}
