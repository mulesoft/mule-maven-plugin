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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;


public class ValidateMojoTest extends MojoTest {

    private static final String MULE_CONFIG_XML = "mule-config.xml";
    private static final String MULE_APP_PROPERTIES = "mule-app.properties";
    private static final String MULE_DEPLOY_PROPERTIES = "mule-deploy.properties";
    private static final String VALIDATE = "validate";
    private static final String EMPTY_PROJECT_NAME = "empty-validate-project";

    public ValidateMojoTest() {
        super("validate-goal-project");
    }

    @Before
    public void before() throws IOException, VerificationException {
        initializeContext();
    }

    @After
    public void after() throws IOException {
        verifier.resetStreams();
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
        builder.exclude(MULE_CONFIG_XML).exclude(MULE_DEPLOY_PROPERTIES).createProjectStructureOfValidateGoal(projectBaseDirectory);
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
