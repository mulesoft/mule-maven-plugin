/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
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
