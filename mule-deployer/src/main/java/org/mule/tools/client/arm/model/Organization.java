/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.client.arm.model;

import java.util.List;

public class Organization {

  public String id;
  public String name;
  public List<Organization> subOrganizations;
}
