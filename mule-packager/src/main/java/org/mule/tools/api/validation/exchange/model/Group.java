/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.validation.exchange.model;

/**
 * Represents an Exchange Group
 *
 * @author Mulesoft Inc.
 * @since 2.0.0
 */
public class Group {

  private String groupId;

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }
}
