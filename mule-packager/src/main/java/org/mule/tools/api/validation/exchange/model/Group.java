/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.exchange.model;/*
                                                     * Copyright (c) 2015 MuleSoft, Inc. This software is protected under international
                                                     * copyright law. All use of this software is subject to MuleSoft's Master Subscription
                                                     * Agreement (or other master license agreement) separately entered into in writing between
                                                     * you and MuleSoft. If such an agreement is not in place, you may not use the software.
                                                     */


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
