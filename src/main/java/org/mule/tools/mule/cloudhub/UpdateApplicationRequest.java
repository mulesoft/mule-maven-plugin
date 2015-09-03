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

    // TODO: ask Dan
    public boolean persistentQueues;
    public boolean monitoringAutoRestart = true;

    public UpdateApplicationRequest()
    {
    }

    public UpdateApplicationRequest(String region, String muleVersion, int workers, String workerType)
    {
        this.region = region;
        this.muleVersion = new MuleVersion();
        this.muleVersion.version = muleVersion;
        this.workers = new Workers();
        this.workers.type = new WorkerType();
        this.workers.type.name = workerType;
        this.workers.amount = workers;
    }

    private static class MuleVersion
    {
        String version;
    }

    private static class Workers
    {
        WorkerType type;
        int amount;
    }

    private static class WorkerType
    {
        String name;
        int weight = 1;
    }
}

