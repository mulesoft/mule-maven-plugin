/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.fabric.model;

import java.util.Date;
import java.util.List;

public class DeploymentGenericResponse {

  public String id;
  public String name;
  public Date lastModifiedDate;
  public Date creationDate;
  public List<String> labels;
  public Target target;
  public String version;
  public String status;
  public AssetReference ref;
}
