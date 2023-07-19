/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.arm.model;

import java.util.Date;

// TODO THIS should be name server or application
public class Data {

  public Data() {

  }

  public int id;
  public Date timeCreated;
  public Date timeUpdated;
  public String desiredStatus;
  public int contextId;
  public String lastReportedStatus;
  public Artifact artifact;
  public Target target;

}
