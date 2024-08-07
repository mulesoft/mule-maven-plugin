/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.mojo;

import integration.ProjectFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.shared.verifier.VerificationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class ValidateMojoTest extends MojoTest {

  protected static final String VALIDATE = "validate";
  private static final String MISSING_DECLARED_SHARED_LIBRARIES_PROJECT = "missing-declared-shared-libraries-project";
  private static final String MISSING_PACKAGE_TYPE_PROJECT = "missing-package-type-project";
  private static final String INVALID_PACKAGE_PROJECT = "invalid-package-project";
  private static final String VALIDATE_SHARED_LIBRARIES_PROJECT = "validate-shared-libraries-project";
  private static final String VALIDATE_DOMAIN_BUNDLE_PROJECT = "validate-domain-bundle-project";


  public ValidateMojoTest() {
    this.goal = VALIDATE;
  }

  @Test
  void testFailOnInvalidPackageType() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(INVALID_PACKAGE_PROJECT, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    String textInLog = "Unknown packaging: mule-invalid";
    executeGoalAndVerifyText(VALIDATE, textInLog);
  }

  @Test
  void testFailWhenMissingPackageType() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(MISSING_PACKAGE_TYPE_PROJECT, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    String textInLog = "Unknown packaging type jar. Please specify a valid mule packaging type:";
    executeGoalAndVerifyText(VALIDATE, textInLog);
  }

  @Test
  void testFailOnMissingSharedLibrariesProject() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(MISSING_DECLARED_SHARED_LIBRARIES_PROJECT, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    String textInLog = "The mule application does not contain the following shared libraries: ";
    executeGoalAndVerifyText(VALIDATE, textInLog);
  }

  @Test
  void testValidateSharedLibrariesProject() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(VALIDATE_SHARED_LIBRARIES_PROJECT, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    try {
      verifier.executeGoal(VALIDATE);
    } catch (VerificationException e) {
      fail(e);
    }
    verifier.verifyErrorFreeLog();
  }

  @Test
  void testProjectWithMunitAsCompileDependency() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir("munit-as-compile", this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    String textInLog = "should have scope 'test'";
    executeGoalAndVerifyText(VALIDATE, textInLog);
  }

  private void executeGoalAndVerifyText(String goal, String text) throws VerificationException {
    try {
      verifier.executeGoal(goal);
    } catch (VerificationException e) {
      if (StringUtils.isNotBlank(text)) {
        verifier.verifyTextInLog(text);
      } else {
        fail(e);
      }
    }
  }

  @Test
  void testValidateBundleDomainWithApplicationReferringToDifferentDomainsProject() throws Exception {
    projectBaseDirectory = ProjectFactory.createProjectBaseDir(VALIDATE_DOMAIN_BUNDLE_PROJECT, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    String textInLog = "Validation exception: Every application in the domain bundle must refer to the specified domain: " +
        "org.mule.app:mule-domain-x:1.0.0:jar:mule-domain. However, the application: " +
        "org.mule.app:mule-app-b:1.0.0:jar:mule-application refers to the following domain(s): " +
        "[org.mule.app:mule-domain-y:1.0.0:jar:mule-domain]";
    executeGoalAndVerifyText(VALIDATE, textInLog);
  }
}

