/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.mulesoft.mule;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.io.IOException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class ExampleIT
{

    private String port = System.getProperty("port");

    @Test
    public void appIsAlive() throws IOException
    {
        HttpGet httpGet = new HttpGet("http://localhost:" + port + "/hello");
        HttpResponse response = new DefaultHttpClient().execute(httpGet);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertEquals("/hello", EntityUtils.toString(response.getEntity()));
    }
    
}
