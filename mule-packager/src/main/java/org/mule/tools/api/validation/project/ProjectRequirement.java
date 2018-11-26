/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.validation.project;

public class ProjectRequirement {

  private boolean disableSemver;
  private boolean strictCheck;

  public ProjectRequirement(boolean disableSemver, boolean isStricCheck) {
    this.disableSemver = disableSemver;
    this.strictCheck = isStricCheck;
  }

  public boolean disableSemver() {
    return disableSemver;
  }

  public boolean isStrictCheck() {
    return strictCheck;
  }

  public static class ProjectRequirementBuilder {

    private boolean disableSemver;
    private boolean strictCheck;

    public ProjectRequirementBuilder withStrictCheck(boolean strictCheck) {
      this.strictCheck = strictCheck;
      return this;
    }

    public ProjectRequirementBuilder withDisableSemver(boolean disableSemver) {
      this.disableSemver = disableSemver;
      return this;
    }

    public ProjectRequirement build() {
      return new ProjectRequirement(disableSemver, strictCheck);
    }
  }
}
