/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.mojo;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mule.runtime.ast.api.ArtifactAst;
import org.mule.runtime.ast.api.ComponentAst;
import org.mule.runtime.ast.api.ComponentMetadataAst;
import org.mule.runtime.ast.api.validation.ValidationResultItem;
import org.mule.runtime.config.internal.validation.SingletonsAreNotRepeated;
import org.mule.tooling.api.AstGenerator;
import org.mule.tooling.api.AstValidatonResult;
import org.mule.tooling.api.ConfigurationException;
import org.mule.tooling.api.DynamicStructureException;
import org.mule.tools.api.packager.packaging.Classifier;
import org.mule.tools.api.packager.sources.ContentGenerator;
import org.mule.tools.api.packager.sources.ContentGeneratorFactory;
import org.mule.tools.api.packager.sources.MuleArtifactContentResolver;
import org.mule.tools.api.packager.sources.MuleContentGenerator;
import org.mule.tools.api.util.MavenProjectInformation;
import org.mule.tools.api.util.Project;
import org.mule.tools.api.validation.project.AbstractProjectValidator;
import org.mule.tools.maven.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mule.tools.api.classloader.model.Artifact.MULE_DOMAIN;
import static org.mule.tools.api.packager.packaging.Classifier.MULE_PLUGIN;
import static org.mule.tools.api.packager.packaging.PackagingType.MULE_POLICY;
import static org.mule.tools.api.packager.structure.PackagerFiles.MULE_ARTIFACT_JSON;
import static org.mule.tools.maven.mojo.AbstractMuleMojoTest.MULE_APPLICATION;

class ProcessClassesMojoTest {

  static class ProcessClassesMojoImpl extends ProcessClassesMojo {

    private NullPointerException nullPointerException;
    private ConfigurationException exception;
    private DynamicStructureException dynamicStructureException;
    private boolean nullArtifact = false;

    void setClassifier(String classifier) {
      this.classifier = classifier;
    }

    void setContentGenerator(ContentGenerator contentGenerator) {
      this.contentGenerator = contentGenerator;
    }

    public void setException(ConfigurationException exception) {
      this.exception = exception;
    }

    public void setNullPointerException(NullPointerException exception) {
      this.nullPointerException = exception;
    }

    public void setDynamicStructureException(DynamicStructureException dynamicStructureException) {
      this.dynamicStructureException = dynamicStructureException;
    }

    public void setNullArtifact(boolean nullArtifact) {
      this.nullArtifact = nullArtifact;
    }

    public void setSkipValidation(boolean skipValidation) {
      this.skipValidation = skipValidation;
    }

    @Override
    public ArtifactAst getArtifactAst()
        throws IOException, ConfigurationException, DynamicStructureException, MojoExecutionException {
      if (exception != null) {
        throw exception;
      }

      if (nullPointerException != null) {
        throw nullPointerException;
      }

      if (dynamicStructureException != null) {
        throw dynamicStructureException;
      }

      if (nullArtifact) {
        return null;
      }

      return super.getArtifactAst();
    }
  }

  private static final String SKIP_AST = "skipAST";
  private static final String SKIP_AST_VALIDATION = "skipASTValidation";

  private static MockedStatic<MavenProjectInformation> MAVEN_PROJECT_INFORMATION;
  private static MockedStatic<ContentGeneratorFactory> CONTENT_GENERATOR_FACTORY;
  private static MockedStatic<AstGenerator> AST_GENERATOR;
  private static final MavenProjectInformation PROJECT_INFORMATION = mock(MavenProjectInformation.class);
  private static final Project PROJECT = mock(Project.class);

  @TempDir
  public File projectBaseFolder;
  @Mock
  private ClassRealm classRealm;
  @Mock
  private PluginDescriptor descriptor;
  @Mock
  private AbstractProjectValidator validator;
  @Spy
  private Map<Object, Object> pluginContext = new HashMap<>();
  @Mock
  private MavenProject project;
  @Mock
  private MavenSession session;
  @Mock
  private MavenExecutionRequest mavenExecutionRequest;
  private MuleContentGenerator muleContentGenerator;

  @InjectMocks
  private ProcessClassesMojoImpl mojo = new ProcessClassesMojoImpl();
  private AutoCloseable autoCloseable;

  @SuppressWarnings("unchecked")
  @BeforeAll
  static void staticSetup() {
    MAVEN_PROJECT_INFORMATION = mockStatic(MavenProjectInformation.class);
    CONTENT_GENERATOR_FACTORY = mockStatic(ContentGeneratorFactory.class);
    AST_GENERATOR = mockStatic(AstGenerator.class);

    MAVEN_PROJECT_INFORMATION
        .when(() -> MavenProjectInformation
            .getProjectInformation(nullable(MavenSession.class), nullable(MavenProject.class), nullable(File.class),
                                   nullable(Boolean.class), nullable(List.class), nullable(String.class), nullable(List.class)))
        .thenReturn(PROJECT_INFORMATION);
  }

  @AfterAll
  static void staticTearDown() {
    MAVEN_PROJECT_INFORMATION.close();
    CONTENT_GENERATOR_FACTORY.close();
    AST_GENERATOR.close();
  }

  @BeforeEach
  void setUp() throws Exception {
    System.getProperties().remove(SKIP_AST);
    System.getProperties().remove(SKIP_AST_VALIDATION);
    //
    AST_GENERATOR.reset();
    reset(PROJECT_INFORMATION);
    autoCloseable = MockitoAnnotations.openMocks(this);
    mojo.setProjectBaseFolder(projectBaseFolder);
    mojo.setSession(session);
    pluginContext.clear();
    // WHEN
    AST_GENERATOR.when(() -> AstGenerator.serialize(nullable(ArtifactAst.class))).thenReturn(mock(InputStream.class));
    AST_GENERATOR.when(() -> AstGenerator.validationResultItemToString(any(ValidationResultItem.class))).thenCallRealMethod();
    when(PROJECT_INFORMATION.getProject()).thenReturn(PROJECT);
    when(PROJECT_INFORMATION.getProjectBaseFolder()).thenReturn(projectBaseFolder.toPath());
    when(PROJECT_INFORMATION.getBuildDirectory()).thenReturn(projectBaseFolder.toPath());
    doNothing().when(classRealm).addURL(any());
    when(descriptor.getClassRealm()).thenReturn(classRealm);
    when(session.getRequest()).thenReturn(mavenExecutionRequest);
    // RESOURCES
    Utils.copyResource(MULE_ARTIFACT_JSON, new File(projectBaseFolder, MULE_ARTIFACT_JSON));
    //
    mojo.setException(null);
    mojo.setNullPointerException(null);
    mojo.setDynamicStructureException(null);
    mojo.setNullArtifact(false);
    muleContentGenerator = new MuleContentGenerator(PROJECT_INFORMATION, mock(Parent.class)) {

      @Override
      public void createAstFile(InputStream inputStream) throws IOException {
        // DO NOTHING
      }

      @Override
      public void copyDescriptorFile() throws IOException {
        // DO NOTHING
      }
    };
  }

  @AfterEach
  void tearDown() throws Exception {
    autoCloseable.close();
  }

  @ParameterizedTest
  @MethodSource("doExecuteNoASTTestValues")
  void doExecuteNoASTTest(Boolean skipAST, String type, boolean withDomain) throws Exception {
    // WHEN
    Optional.ofNullable(skipAST).ifPresent(value -> System.setProperty(SKIP_AST, String.valueOf(value)));
    when(validator.isProjectValid(anyString())).thenReturn(true);
    setProject(type, withDomain);
    // THEN
    mojo.setContentGenerator(muleContentGenerator);
    mojo.execute();
    // VALIDATION
    verify(validator).isProjectValid(anyString());
  }

  static Stream<Arguments> doExecuteNoASTTestValues() {
    return Stream.of(
                     Arguments.of(true, MULE_APPLICATION, false),
                     Arguments.of(null, MULE_POLICY.toString(), false),
                     Arguments.of(false, MULE_APPLICATION, true));
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest
  @MethodSource("doExecuteTestValues")
  void doExecuteTest(Boolean skipAST, String packaging, Boolean withDomain, boolean nullArtifact) throws Exception {
    // RESOURCES
    Utils.copyResource("mule-artifact-4.6.0.json",
                       new File(projectBaseFolder, MULE_ARTIFACT_JSON));
    // VALUES
    ArtifactAst artifactAST = mock(ArtifactAst.class);
    // WHEN
    Optional.ofNullable(skipAST).ifPresent(value -> System.setProperty(SKIP_AST, String.valueOf(value)));
    when(validator.isProjectValid(anyString())).thenReturn(true);
    setProject(packaging, withDomain);
    // THEN
    try (MockedConstruction<AstGenerator> astGenerator = mockConstruction(AstGenerator.class, (mock, context) -> {
      when(mock.generateAST(nullable(String.class), nullable(List.class), nullable(Path.class))).thenReturn(artifactAST);
    });
        MockedConstruction<MuleArtifactContentResolver> contentResolver =
            mockConstruction(MuleArtifactContentResolver.class, (mock, context) -> {
            })) {

      mojo.setNullArtifact(nullArtifact);
      mojo.setContentGenerator(muleContentGenerator);
      mojo.setClassifier(MULE_PLUGIN.toString());
      mojo.execute();
    }
    // VALIDATION
    verify(validator).isProjectValid(anyString());
  }

  static Stream<Arguments> doExecuteTestValues() {
    return Stream.of(
                     Arguments.of(null, MULE_APPLICATION, false, false),
                     Arguments.of(null, MULE_APPLICATION, null, false),
                     Arguments.of(null, MULE_APPLICATION, null, true),
                     Arguments.of(false, MULE_APPLICATION, false, false));
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest
  @MethodSource("getArtifactAstTestValues")
  void getArtifactAstTest(Boolean skipASTValidation, boolean minVersion, ArtifactAst artifactAST,
                          AstValidatonResult validationResult, Classifier classifier, String packaging, boolean exception)
      throws Exception {
    // RESOURCES
    Utils.copyResource("mule-artifact-" + (minVersion ? "4.3.0" : "4.6.0") + ".json",
                       new File(projectBaseFolder, MULE_ARTIFACT_JSON));
    // VALUES
    Optional.ofNullable(skipASTValidation).ifPresent(value -> System.setProperty(SKIP_AST_VALIDATION, String.valueOf(value)));
    // WHEN
    setProject(packaging, false);
    // THEN
    try (MockedConstruction<AstGenerator> astGenerator = mockConstruction(AstGenerator.class, (mock, context) -> {
      when(mock.generateAST(nullable(String.class), nullable(List.class), nullable(Path.class))).thenReturn(artifactAST);
      when(mock.validateAST(nullable(ArtifactAst.class))).thenReturn(validationResult);
    });
        MockedConstruction<MuleArtifactContentResolver> contentResolver =
            mockConstruction(MuleArtifactContentResolver.class, (mock, context) -> {
            })) {
      mojo.setClassifier(classifier.toString());
      if (exception) {
        assertThatThrownBy(() -> mojo.getArtifactAst()).isInstanceOf(DynamicStructureException.class);
      } else {
        assertThat(artifactAST).isEqualTo(mojo.getArtifactAst());
      }
    }
  }

  static Stream<Arguments> getArtifactAstTestValues() {
    ComponentAst componentAst = mock(ComponentAst.class);
    when(componentAst.getMetadata()).thenReturn(ComponentMetadataAst.EMPTY_METADATA);
    List<ValidationResultItem> validationResultItems =
        Collections.singletonList(ValidationResultItem.create(componentAst, new SingletonsAreNotRepeated(), "NO MESSAGE"));

    AstValidatonResult validationResultNoItems =
        new AstValidatonResult(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    AstValidatonResult validationResultWarn =
        new AstValidatonResult(Collections.emptyList(), validationResultItems, Collections.emptyList());
    AstValidatonResult validationResultError =
        new AstValidatonResult(validationResultItems, validationResultItems, validationResultItems);

    assertThat(validationResultNoItems.getErrors()).isEmpty();
    assertThat(validationResultError.getErrors()).isNotEmpty();
    AstValidatonResult validationResult = mock(AstValidatonResult.class);
    ArtifactAst artifactAST = mock(ArtifactAst.class);
    return Stream.of(
                     Arguments.of(true, false, null, validationResult, Classifier.MULE_APPLICATION, MULE_APPLICATION, false),
                     Arguments.of(false, false, null, validationResult, Classifier.MULE_APPLICATION, MULE_APPLICATION, false),
                     Arguments.of(null, false, null, validationResult, Classifier.MULE_APPLICATION, MULE_APPLICATION, false),
                     Arguments.of(true, true, artifactAST, validationResult, Classifier.MULE_APPLICATION, MULE_APPLICATION,
                                  false),
                     Arguments.of(null, false, artifactAST, validationResult, MULE_PLUGIN, MULE_APPLICATION, false),
                     Arguments.of(false, false, artifactAST, validationResultNoItems,
                                  Classifier.MULE_APPLICATION, MULE_APPLICATION, false),
                     Arguments.of(false, false, artifactAST, validationResultWarn, Classifier.MULE_APPLICATION,
                                  MULE_APPLICATION, false),
                     Arguments.of(false, true, artifactAST, validationResultError, Classifier.MULE_APPLICATION, MULE_APPLICATION,
                                  true));
  }

  @ParameterizedTest
  @MethodSource("exceptionTestValues")
  void exceptionTest(Consumer<ProcessClassesMojoImpl> consumer, Class<?> clazz, String message, ContentGenerator contentGenerator)
      throws MojoExecutionException, MojoFailureException {
    consumer.accept(mojo);
    mojo.setSkipValidation(true);
    mojo.setContentGenerator(contentGenerator);
    setProject(MULE_APPLICATION, false);

    if (clazz == null) {
      mojo.execute();
    } else {
      assertThatThrownBy(mojo::execute).isInstanceOf(clazz).hasMessageContaining(message);
    }
  }

  static Stream<Arguments> exceptionTestValues() throws IOException {
    StackTraceElement stackTraceElement = mock(StackTraceElement.class);
    when(stackTraceElement.getClassName()).thenReturn("org.mule.runtime.config.internal.validation.AbstractErrorTypesValidation");
    when(stackTraceElement.getMethodName()).thenReturn("isErrorTypePresent");

    NullPointerException nullPointerException = mock(NullPointerException.class);
    when(nullPointerException.getStackTrace()).thenReturn(new StackTraceElement[] {stackTraceElement, stackTraceElement});

    ContentGenerator contentGenerator = mock(ContentGenerator.class);
    ContentGenerator contentGeneratorWithError = mock(ContentGenerator.class);
    doThrow(new IOException()).when(contentGeneratorWithError).copyDescriptorFile();

    return Stream.of(
                     Arguments.of((Consumer<ProcessClassesMojoImpl>) mojo -> mojo
                         .setDynamicStructureException(new DynamicStructureException()), null, null, contentGenerator),
                     Arguments.of((Consumer<ProcessClassesMojoImpl>) mojo -> mojo.setException(new ConfigurationException("")),
                                  MojoFailureException.class, "Fail to compile", contentGenerator),
                     Arguments
                         .of((Consumer<ProcessClassesMojoImpl>) mojo -> mojo.setNullPointerException(nullPointerException),
                             null, null, contentGenerator),
                     Arguments
                         .of((Consumer<ProcessClassesMojoImpl>) mojo -> mojo.setNullPointerException(new NullPointerException()),
                             MojoFailureException.class, "Fail to compile", contentGenerator),
                     Arguments
                         .of((Consumer<ProcessClassesMojoImpl>) mojo -> mojo.setNullArtifact(true),
                             MojoExecutionException.class, "process-classes exception", contentGeneratorWithError));
  }

  private void setProject(String packaging, Boolean withDomains) {
    Build build = mock(Build.class);
    reset(project);

    when(build.getDirectory()).thenReturn(projectBaseFolder.getAbsolutePath());
    when(project.getPackaging()).thenReturn(packaging);
    when(project.getBasedir()).thenReturn(projectBaseFolder);
    when(project.getBuild()).thenReturn(build);
    when(project.getModel()).thenReturn(mock(Model.class));

    if (withDomains == null) {
      when(project.getDependencies()).thenReturn(null);
    } else if (withDomains) {
      List<Dependency> dependencies = new ArrayList<>(3);
      dependencies.add(createDependency(null));
      dependencies.add(createDependency(MULE_APPLICATION));
      dependencies.add(createDependency(MULE_DOMAIN));
      when(project.getDependencies()).thenReturn(dependencies);
    } else {
      when(project.getDependencies()).thenReturn(Collections.emptyList());
    }

  }

  private Dependency createDependency(String classifier) {
    Dependency dependency = mock(Dependency.class);
    when(dependency.getClassifier()).thenReturn(classifier);
    return dependency;
  }
}
