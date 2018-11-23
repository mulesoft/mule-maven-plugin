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

  private boolean enforceSemver;
  private boolean strictCheck;

  public ProjectRequirement(boolean enforceSemver, boolean isStricCheck) {
    this.enforceSemver = enforceSemver;
    this.strictCheck = isStricCheck;
  }

  public boolean enforceSemver() {
    return enforceSemver;
  }

  public boolean isStrictCheck() {
    return strictCheck;
  }

  public static class ProjectRequirementBuilder {

    private boolean enforceSemver;
    private boolean strictCheck;

    public ProjectRequirementBuilder withStrictCheck(boolean strictCheck) {
      this.strictCheck = strictCheck;
      return this;
    }

    public ProjectRequirementBuilder withSemverEnforcement(boolean enforceSemver) {
      this.enforceSemver = enforceSemver;
      return this;
    }

    public ProjectRequirement build() {
      return new ProjectRequirement(enforceSemver, strictCheck);
    }
  }
}
