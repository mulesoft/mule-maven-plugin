/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model.anypoint;

public class Runtime {

  protected String version;

  protected String releaseChannel;

  protected String java;

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getReleaseChannel() {
    return releaseChannel;
  }

  public void setReleaseChannel(String releaseChannel) {
    this.releaseChannel = releaseChannel;
  }

  public String getJava() {
    return java;
  }

  public void setJava(String java) {
    this.java = java;
  }
}
