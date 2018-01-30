/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.client.cloudhub.model;

import java.util.Map;

/**
 * @author Mulesoft Inc.
 * @since 3.2.0
 */
public class LatestUpdate {

  private String id;
  private String name;
  private Long releaseDate;
  private String releaseNotes;
  private Map<String, Boolean> flags;
  private String muleDistributionName;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getReleaseDate() {
    return releaseDate;
  }

  public void setReleaseDate(Long releaseDate) {
    this.releaseDate = releaseDate;
  }

  public String getReleaseNotes() {
    return releaseNotes;
  }

  public void setReleaseNotes(String releaseNotes) {
    this.releaseNotes = releaseNotes;
  }

  public Map<String, Boolean> getFlags() {
    return flags;
  }

  public void setFlags(Map<String, Boolean> flags) {
    this.flags = flags;
  }

  public String getMuleDistributionName() {
    return muleDistributionName;
  }

  public void setMuleDistributionName(String muleDistributionName) {
    this.muleDistributionName = muleDistributionName;
  }
}
