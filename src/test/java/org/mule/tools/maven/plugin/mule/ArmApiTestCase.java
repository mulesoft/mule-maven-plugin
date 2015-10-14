/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.mule.tools.maven.plugin.mule.arm.ArmApi;
import org.mule.tools.maven.plugin.mule.arm.Applications;
import org.mule.tools.maven.plugin.mule.arm.Environment;
import org.mule.tools.maven.plugin.mule.arm.Target;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ArmApiTestCase
{

    private static final String USERNAME = System.getProperty("username");
    private static final String PASSWORD = System.getProperty("password");
    private static final String ENVIRONMENT = "Production";
    private ArmApi armApi;

    @Before
    public void setup()
    {
        armApi = new ArmApi(null, "https://anypoint.mulesoft.com", USERNAME, PASSWORD, ENVIRONMENT, "");
        armApi.init();
    }

    @Test
    public void getApplications()
    {
        Applications apps = armApi.getApplications();
        assertThat(apps, notNullValue());
    }

    @Test
    public void findEnvironmentByName()
    {
        Environment environment = armApi.findEnvironmentByName("Production");
        assertThat(environment.name, equalTo("Production"));
    }

    @Test(expected = RuntimeException.class)
    public void failToFindFakeEnvironment()
    {
        armApi.findEnvironmentByName("notProduction");
    }

    @Test
    public void findServerByName()
    {
        Target target = armApi.findServerByName("server-name");
        assertThat(target.name, equalTo("server-name"));
    }

    @Test(expected = RuntimeException.class)
    public void failToFindFakeTargetName()
    {
        armApi.findServerByName("fake-server-name");
    }

    private Callable<Boolean> appIsStarted(final int applicationId)
    {
        return new Callable<Boolean>()
        {
            @Override
            public Boolean call() throws Exception
            {
                return armApi.isStarted(applicationId);
            }
        };
    }

}
