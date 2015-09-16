/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.plugin.mule.arm;

import java.util.Date;

public class Data
{

    public Data()
    {

    }

    public int id;
    public Date timeCreated;
    public Date timeUpdated;
    public String desiredStatus;
    public int contextId;
    public String lastReportedStatus;
    public Artifact artifact;

}
