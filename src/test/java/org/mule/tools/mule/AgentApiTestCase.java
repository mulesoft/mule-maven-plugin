/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.mule;

import org.mule.tools.mule.agent.AgentApi;

import java.io.File;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class AgentApiTestCase
{

    private static final File APP = new File("/tmp/echo-test4.zip");
    private AgentApi agentApi;

    @Before
    public void setup()
    {
        agentApi = new AgentApi(null, "http://localhost:9999/");
    }

    @Test
    public void deployApplication()
    {
        agentApi.deployApplication("test", APP);
    }

    @Test
    public void undeployApplication()
    {
        agentApi.undeployApplication("echo-test4");
    }

}
