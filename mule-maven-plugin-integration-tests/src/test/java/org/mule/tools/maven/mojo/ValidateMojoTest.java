/**
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.junit.Test;

public class ValidateMojoTest extends MojoTest {
    protected static final String VALIDATE = "validate";

    public ValidateMojoTest() {
        this.goal = VALIDATE;
    }

    @Test
    public void testFailOnEmptyProject() throws Exception {
        projectBaseDirectory = builder.createProjectBaseDir(EMPTY_PROJECT_NAME, this.getClass());
        verifier = new Verifier(projectBaseDirectory.getAbsolutePath());
        try {
            verifier.executeGoal(VALIDATE);
        } catch (VerificationException e) {
        }
        verifier.verifyTextInLog("Invalid Mule project. Missing src/main/mule folder. This folder is mandatory");
    }

    @Test
    public void testFailWhenJustContainsSrcFolder() throws Exception {
        builder.exclude(MULE_APP_PROPERTIES).exclude(MULE_CONFIG_XML).exclude(MULE_DEPLOY_PROPERTIES).createProjectStructureOfValidateGoal(projectBaseDirectory);
        try {
            verifier.executeGoal(VALIDATE);
        } catch (VerificationException e) {
        }
        verifier.verifyTextInLog("Invalid Mule project. Missing mule-app.properties file, it must be present in the root of application");
    }

    @Test
    public void testFailWhenDoesNotContainsConfigNeitherDeployProperties() throws Exception {
        builder.exclude(MULE_CONFIG_XML).exclude(MULE_DEPLOY_PROPERTIES).exclude(MULE_APP_JSON).createProjectStructureOfValidateGoal(projectBaseDirectory);
        try {
            verifier.executeGoal(VALIDATE);
        } catch(VerificationException e) {}
        verifier.verifyTextInLog("Invalid Mule project. Either mule-deploy.properties or mule-config.xml files must be present in the root of application");
    }

    @Test
    public void testSucceedWhenDoesNotContainsDeployProperties() throws Exception {
        builder.exclude(MULE_DEPLOY_PROPERTIES).createProjectStructureOfValidateGoal(projectBaseDirectory);
        verifier.executeGoal(VALIDATE);
        verifier.verifyErrorFreeLog();
    }

    @Test
    public void testSucceedWhenDoesNotContainsConfigProperties() throws Exception {
        builder.exclude(MULE_CONFIG_XML).createProjectStructureOfValidateGoal(projectBaseDirectory);
        verifier.executeGoal(VALIDATE);
        verifier.verifyErrorFreeLog();
    }
}
