/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integrationTests.mojo;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.it.VerificationException;
import org.junit.Test;

public class ValidateMojoTest extends MojoTest {

  protected static final String VALIDATE = "validate";
  private static final String MISSING_DECLARED_SHARED_LIBRARIES_PROJECT = "missing-declared-shared-libraries-project";
  private static final String MISSING_PACKAGE_TYPE_PROJECT = "missing-package-type-project";
  private static final String INVALID_PACKAGE_PROJECT = "invalid-package-project";
  private static final String VALIDATE_SHARED_LIBRARIES_PROJECT = "validate-shared-libraries-project";


  public ValidateMojoTest() {
    this.goal = VALIDATE;
  }

  @Test(expected = VerificationException.class)
  public void testFailOnEmptyProject() throws Exception {
    projectBaseDirectory = builder.createProjectBaseDir(EMPTY_PROJECT_NAME, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    verifier.executeGoal(VALIDATE);
    // verifier.verifyTextInLog("Invalid Mule project. Missing src/main/mule folder. This folder is mandatory");
  }

  @Test
  public void testFailOnInvalidPackageType() throws Exception {
    projectBaseDirectory = builder.createProjectBaseDir(INVALID_PACKAGE_PROJECT, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    String textInLog = "Unknown packaging: mule-invalid";
    executeGoalAndVerifyText(VALIDATE, textInLog);
  }

  @Test
  public void testFailWhenMissingPackageType() throws Exception {
    projectBaseDirectory = builder.createProjectBaseDir(MISSING_PACKAGE_TYPE_PROJECT, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    String textInLog = "Unknown packaging type jar. Please specify a valid mule packaging type:";
    executeGoalAndVerifyText(VALIDATE, textInLog);
  }

  @Test(expected = VerificationException.class)
  public void testFailOnEmptyPolicyProject() throws Exception {
    projectBaseDirectory = builder.createProjectBaseDir(EMPTY_POLICY_NAME, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.executeGoal(VALIDATE);
    // verifier.verifyTextInLog("Invalid Mule project. Missing src/main/policy folder. This folder is mandatory");
  }

  @Test(expected = VerificationException.class)
  public void testFailOnEmptyDomainProject() throws Exception {
    projectBaseDirectory = builder.createProjectBaseDir(EMPTY_DOMAIN_NAME, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    verifier.executeGoal(VALIDATE);
    // verifier.verifyTextInLog("Invalid Mule project. Missing src/main/mule folder. This folder is mandatory");
  }

  @Test
  public void testFailOnMissingSharedLibrariesProject() throws Exception {
    projectBaseDirectory = builder.createProjectBaseDir(MISSING_DECLARED_SHARED_LIBRARIES_PROJECT, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);

    String textInLog = "The mule application does not contain the following shared libraries: ";
    executeGoalAndVerifyText(VALIDATE, textInLog);
  }

  @Test
  public void testValidateSharedLibrariesProject() throws Exception {
    installThirdPartyArtifact(DEPENDENCY_A_GROUP_ID, DEPENDENCY_A_ARTIFACT_ID, DEPENDENCY_A_VERSION, DEPENDENCY_A_TYPE,
                              DEPENDENCY_A_PROJECT_NAME);
    installThirdPartyArtifact(DEPENDENCY_B_GROUP_ID, DEPENDENCY_B_ARTIFACT_ID, DEPENDENCY_B_VERSION, DEPENDENCY_B_TYPE,
                              DEPENDENCY_B_PROJECT_NAME);

    projectBaseDirectory = builder.createProjectBaseDir(VALIDATE_SHARED_LIBRARIES_PROJECT, this.getClass());
    verifier = buildVerifier(projectBaseDirectory);
    try {
      verifier.executeGoal(VALIDATE);
    } catch (VerificationException e) {
    }
    verifier.verifyErrorFreeLog();
  }

  private void executeGoalAndVerifyText(String goal, String text) throws VerificationException {
    try {
      verifier.executeGoal(goal);
    } catch (VerificationException e) {
      if (StringUtils.isNotBlank(text)) {
        verifier.verifyTextInLog(text);
      } else {
        throw e;
      }
    }
  }


}
