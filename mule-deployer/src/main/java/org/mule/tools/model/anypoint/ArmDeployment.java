/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model.anypoint;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mule.tools.utils.VersionUtils.isGreaterThanOrSameVersion;

import org.apache.maven.plugins.annotations.Parameter;

import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.model.TargetType;

import java.util.Map;

public class ArmDeployment extends AnypointDeployment {

  private static final String FIRST_ARM_VERSION_THAT_ACCEPTS_PROPERTIES = "3.9.0";
  @Parameter
  protected String target;

  @Parameter
  protected TargetType targetType;

  @Parameter
  protected Boolean armInsecure;

  @Parameter
  protected Boolean failIfNotExists;

  @Parameter
  protected Map<String, String> properties;

  /**
   * Anypoint Platform target name.
   *
   * @since 2.0
   */
  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  /**
   * Anypoint Platform target type: server, serverGroup or cluster.
   *
   * @since 2.0
   */
  public TargetType getTargetType() {
    return targetType;
  }

  public void setTargetType(TargetType targetType) {
    this.targetType = targetType;
  }

  /**
   * Use insecure mode for ARM deploymentConfiguration: do not validate certificates, nor hostname.
   *
   * @since 2.1
   */
  public Boolean isArmInsecure() {
    return armInsecure;
  }

  public void setArmInsecure(boolean armInsecure) {
    this.armInsecure = armInsecure;
  }

  /**
   * When set to false, undeployment won't fail if the specified application does not exist.
   *
   * @since 2.2
   */
  public Boolean isFailIfNotExists() {
    return failIfNotExists;
  }

  public void setFailIfNotExists(boolean failIfNotExists) {
    this.failIfNotExists = failIfNotExists;
  }


  /**
   * Properties map. Available in versions greater than or equals to {@code FIRST_ARM_VERSION_THAT_ACCEPTS_PROPERTIES}.
   *
   * @return map of properties
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }

  public void setEnvironmentSpecificValues() throws DeploymentException {
    super.setEnvironmentSpecificValues();

    String isArmInsecure = getProperty("arm.insecure");
    if (isNotBlank(isArmInsecure)) {
      setArmInsecure(Boolean.valueOf(isArmInsecure));
    }
    if (isArmInsecure() == null) {
      setArmInsecure(false);
    }

    if (isFailIfNotExists() == null) {
      setFailIfNotExists(Boolean.TRUE);
    }

    String anypointTarget = getProperty("anypoint.target");
    if (isNotBlank(anypointTarget)) {
      setTarget(anypointTarget);
    }

    String targetType = getProperty("anypoint.target.type");
    if (isNotBlank(targetType)) {
      setTargetType(TargetType.valueOf(targetType));
    }

    if (!isGreaterThanOrSameVersion(muleVersion, FIRST_ARM_VERSION_THAT_ACCEPTS_PROPERTIES)) {
      if (properties != null) {
        throw new DeploymentException("Properties are not allowed. Mule Runtime version should be at least "
            + FIRST_ARM_VERSION_THAT_ACCEPTS_PROPERTIES);
      }
    }
  }
}
