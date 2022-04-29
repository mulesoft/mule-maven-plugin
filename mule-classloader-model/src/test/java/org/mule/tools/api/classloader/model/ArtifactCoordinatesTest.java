package org.mule.tools.api.classloader.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ArtifactCoordinatesTest {

  private static final String ARTIFACT_ID = "artifact-id";
  private static final String VERSION = "1.0.0";
  private static final String TYPE = "zip";
  private static final String CLASSIFIER = "classifier";
  private static final String MULE_DOMAIN = "mule-domain";
  private static final String PREFIX_GROUP_ID = "group";
  private static final String SUFFIX_GROUP_ID = "id";
  private static final String GROUP_ID = PREFIX_GROUP_ID + "." + SUFFIX_GROUP_ID;

  @DisplayName("Checking the constructors.")
  @Test
  public void constructorsTests() {
    List<String> emptyValues = Arrays.asList(null, "", "   ");

    // EMPTY GROUP ID
    emptyValues.forEach(groupId -> {
      Throwable thrown = assertThrows(IllegalArgumentException.class,
                                      () -> new ArtifactCoordinates(groupId, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER));
      assertThat(thrown.getMessage(), is("Group id cannot be null nor blank"));
      assertThat(thrown, instanceOf(IllegalArgumentException.class));
    });

    // EMPTY ARTIFACT ID
    emptyValues.forEach(artifactId -> {
      Throwable thrown = assertThrows(IllegalArgumentException.class,
                                      () -> new ArtifactCoordinates(GROUP_ID, artifactId, VERSION, TYPE, CLASSIFIER));
      assertThat(thrown.getMessage(), is("Artifact id can not be null nor blank"));
      assertThat(thrown, instanceOf(IllegalArgumentException.class));
    });

    // EMPTY VERSION ID
    emptyValues.forEach(version -> {
      Throwable thrown = assertThrows(IllegalArgumentException.class,
                                      () -> new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, version, TYPE, CLASSIFIER));
      assertThat(thrown.getMessage(), is("Version can not be null nor blank"));
      assertThat(thrown, instanceOf(IllegalArgumentException.class));
    });

    // EMPTY VERSION ID
    emptyValues.forEach(type -> {
      Throwable thrown = assertThrows(IllegalArgumentException.class,
                                      () -> new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, type, CLASSIFIER));
      assertThat(thrown.getMessage(), is("Type can not be null nor blank"));
      assertThat(thrown, instanceOf(IllegalArgumentException.class));
    });

    // VALID VALUES
    Arrays.asList(null, "", "   ", "test", "runtime").forEach(scope -> {
      String classifier = Optional.ofNullable(scope).map(value -> CLASSIFIER + value).orElse(null);
      ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, classifier, scope);
      assertThat(artifactCoordinates.getGroupId(), equalTo(GROUP_ID));
      assertThat(artifactCoordinates.getArtifactId(), equalTo(ARTIFACT_ID));
      assertThat(artifactCoordinates.getVersion(), equalTo(VERSION));
      assertThat(artifactCoordinates.getType(), equalTo(TYPE));
      assertThat(artifactCoordinates.getClassifier(), equalTo(classifier));
      assertThat(artifactCoordinates.getScope(), equalTo(scope));
    });

    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION);
    assertThat(artifactCoordinates.getGroupId(), equalTo(GROUP_ID));
    assertThat(artifactCoordinates.getArtifactId(), equalTo(ARTIFACT_ID));
    assertThat(artifactCoordinates.getVersion(), equalTo(VERSION));
    assertThat(artifactCoordinates.getType(), equalTo(ArtifactCoordinates.DEFAULT_ARTIFACT_TYPE));
    assertThat(artifactCoordinates.getClassifier(), nullValue());
    assertThat(artifactCoordinates.getScope(), nullValue());
  }

  @DisplayName("Set groupId tests.")
  @Test
  public void setGroupIdTests() {
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);

    // SETTING EMPTY GROUP ID
    Arrays.asList(null, "", "   ").forEach(groupId -> {
      Throwable thrown = assertThrows(IllegalArgumentException.class, () -> artifactCoordinates.setGroupId(groupId));
      assertThat(thrown.getMessage(), is("Group id cannot be null nor blank"));
      assertThat(thrown, instanceOf(IllegalArgumentException.class));
    });

    // SETTING VALID GROUP ID
    String newGroupId = UUID.randomUUID().toString();
    ArtifactCoordinates newArtifactCoordinates = artifactCoordinates.setGroupId(newGroupId);

    assertThat(newArtifactCoordinates.getGroupId(), equalTo(newGroupId));
    assertThat(artifactCoordinates.getGroupId(), not(equalTo(newGroupId)));
    assertThat(artifactCoordinates.getGroupId(), not(equalTo(newArtifactCoordinates.getGroupId())));
    assertThat(artifactCoordinates, not(equalTo(newArtifactCoordinates)));
  }

  @DisplayName("Set artifactId tests.")
  @Test
  public void setArtifactIdTests() {
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);

    // SETTING EMPTY ARTIFACT ID
    Arrays.asList(null, "", "   ").forEach(artifactId -> {
      Throwable thrown = assertThrows(IllegalArgumentException.class, () -> artifactCoordinates.setArtifactId(artifactId));
      assertThat(thrown.getMessage(), is("Artifact id can not be null nor blank"));
      assertThat(thrown, instanceOf(IllegalArgumentException.class));
    });

    // SETTING VALID ARTIFACT ID
    String newArtifactId = UUID.randomUUID().toString();
    ArtifactCoordinates newArtifactCoordinates = artifactCoordinates.setArtifactId(newArtifactId);

    assertThat(newArtifactCoordinates.getArtifactId(), equalTo(newArtifactId));
    assertThat(artifactCoordinates.getArtifactId(), not(equalTo(newArtifactId)));
    assertThat(artifactCoordinates.getArtifactId(), not(equalTo(newArtifactCoordinates.getArtifactId())));
    assertThat(artifactCoordinates, not(equalTo(newArtifactCoordinates)));
  }

  @DisplayName("Set version tests.")
  @Test
  public void setVersionTests() {
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);

    // SETTING EMPTY VERSION
    Arrays.asList(null, "", "   ").forEach(version -> {
      Throwable thrown = assertThrows(IllegalArgumentException.class, () -> artifactCoordinates.setVersion(version));
      assertThat(thrown.getMessage(), is("Version can not be null nor blank"));
      assertThat(thrown, instanceOf(IllegalArgumentException.class));
    });

    // SETTING VALID VERSION
    String newVersion = UUID.randomUUID().toString();
    ArtifactCoordinates newArtifactCoordinates = artifactCoordinates.setVersion(newVersion);

    assertThat(newArtifactCoordinates.getVersion(), equalTo(newVersion));
    assertThat(artifactCoordinates.getVersion(), not(equalTo(newVersion)));
    assertThat(artifactCoordinates.getVersion(), not(equalTo(newArtifactCoordinates.getVersion())));
    assertThat(artifactCoordinates, not(equalTo(newArtifactCoordinates)));
  }

  @DisplayName("Set type tests.")
  @Test
  public void setTypeTests() {
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);

    // SETTING EMPTY TYPE
    Arrays.asList(null, "", "   ").forEach(type -> {
      Throwable thrown = assertThrows(IllegalArgumentException.class, () -> artifactCoordinates.setType(type));
      assertThat(thrown.getMessage(), is("Type can not be null nor blank"));
      assertThat(thrown, instanceOf(IllegalArgumentException.class));
    });

    // SETTING VALID TYPE
    String newType = UUID.randomUUID().toString();
    ArtifactCoordinates newArtifactCoordinates = artifactCoordinates.setType(newType);

    assertThat(newArtifactCoordinates.getType(), equalTo(newType));
    assertThat(artifactCoordinates.getType(), not(equalTo(newType)));
    assertThat(artifactCoordinates.getType(), not(equalTo(newArtifactCoordinates.getType())));
    assertThat(artifactCoordinates, equalTo(newArtifactCoordinates));
  }

  @DisplayName("Set classifier test.")
  @Test
  public void setClassifierTest() {
    ArtifactCoordinates artifactCoordinates = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);

    // SETTING VALID CLASSIFIER
    String newClassifier = UUID.randomUUID().toString();
    ArtifactCoordinates newArtifactCoordinates = artifactCoordinates.setClassifier(newClassifier);

    assertThat(newArtifactCoordinates.getClassifier(), equalTo(newClassifier));
    assertThat(artifactCoordinates.getClassifier(), not(equalTo(newClassifier)));
    assertThat(artifactCoordinates.getClassifier(), not(equalTo(newArtifactCoordinates.getClassifier())));
    assertThat(artifactCoordinates, not(equalTo(newArtifactCoordinates)));
  }

  @DisplayName("Set scope test.")
  @Test
  public void setScopeTest() {
    ArtifactCoordinates artifactCoordinates =
        new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER, UUID.randomUUID().toString());

    // SETTING VALID SCOPE
    Arrays.asList(null, "", "   ", "test", "runtime", UUID.randomUUID().toString()).forEach(scope -> {
      ArtifactCoordinates newArtifactCoordinates = artifactCoordinates.setScope(scope);
      assertThat(newArtifactCoordinates.getScope(), equalTo(scope));
      assertThat(artifactCoordinates.getScope(), not(equalTo(scope)));
      assertThat(artifactCoordinates.getScope(), not(equalTo(newArtifactCoordinates.getScope())));
      assertThat(artifactCoordinates, equalTo(newArtifactCoordinates));
    });
  }

  @DisplayName("ToString tests.")
  @Test
  public void toStringTests() {
    // NOT NULL CLASSIFIER
    ArtifactCoordinates artifactCoordinates01 = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    String expected01 = String.join(":", GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);

    assertThat(artifactCoordinates01.toString(), equalTo(expected01));
    assertThat(String.valueOf(artifactCoordinates01), equalTo(expected01));

    //NULL CLASSIFIER
    ArtifactCoordinates artifactCoordinates02 = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, null);
    String expected02 = String.join(":", GROUP_ID, ARTIFACT_ID, VERSION, TYPE);

    assertThat(artifactCoordinates02.toString(), equalTo(expected02));
    assertThat(String.valueOf(artifactCoordinates02), equalTo(expected02));
  }

  @DisplayName("Equals tests.")
  @Test
  public void equalsTests() {
    ArtifactCoordinates artifactCoordinates01 = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    ArtifactCoordinates artifactCoordinates02 = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER);
    ArtifactCoordinates artifactCoordinates03 = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, MULE_DOMAIN);

    assertThat(artifactCoordinates01.equals(artifactCoordinates01), equalTo(true));
    assertThat(artifactCoordinates01.equals(null), equalTo(false));
    assertThat(artifactCoordinates01.equals(artifactCoordinates02), equalTo(true));
    assertThat(artifactCoordinates02.equals(artifactCoordinates01), equalTo(true));
    assertThat(artifactCoordinates02.equals(artifactCoordinates03), equalTo(false));
  }

  @DisplayName("HashCode tests.")
  @Test
  public void hashCodeTests() {
    int hashCode01 = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER).hashCode();
    int hashCode02 = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, CLASSIFIER).hashCode();
    int hashCode03 = new ArtifactCoordinates(GROUP_ID, ARTIFACT_ID, VERSION, TYPE, MULE_DOMAIN).hashCode();

    assertThat(hashCode01, equalTo(hashCode02));
    assertThat(hashCode02, not(equalTo(hashCode03)));
    assertThat(hashCode01, not(equalTo(hashCode03)));
  }
}
