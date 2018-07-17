/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
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
