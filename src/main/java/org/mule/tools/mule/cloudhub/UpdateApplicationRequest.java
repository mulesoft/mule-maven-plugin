/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.mule.cloudhub;

public class UpdateApplicationRequest
{

    public String region;
    public MuleVersion muleVersion;
    public Workers workers;

    public UpdateApplicationRequest()
    {
    }

    public UpdateApplicationRequest(String region, String muleVersion, Integer workers, String workerType)
    {
        this.region = region;
        this.muleVersion = new MuleVersion();
        this.muleVersion.version = muleVersion;
        this.workers = new Workers();
        this.workers.type = new WorkerType();
        this.workers.type.name = workerType;
        this.workers.amount = workers;
    }

    public static class MuleVersion
    {
        public String version;
    }

    public static class Workers
    {
        public WorkerType type;
        public Integer amount;
    }

    public static class WorkerType
    {
        public String name;
        //int weight = 1;
    }
}

