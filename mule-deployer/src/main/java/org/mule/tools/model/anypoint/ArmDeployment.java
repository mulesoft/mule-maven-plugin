/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.model.anypoint;

import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.client.core.exception.DeploymentException;
import org.mule.tools.client.model.TargetType;

import java.util.Optional;

import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


public class ArmDeployment extends AnypointDeployment {

  @Parameter
  protected String target;

  @Parameter
  protected TargetType targetType;

  @Parameter
  protected Boolean armInsecure;

  @Parameter
  protected Boolean failIfNotExists;

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
  public Optional<Boolean> isArmInsecure() {
    return Optional.ofNullable(armInsecure);
  }

  public void setArmInsecure(boolean armInsecure) {
    this.armInsecure = armInsecure;
  }

  /**
   * When set to false, undeployment won't fail if the specified application does not exist.
   *
   * @since 2.2
   */
  public Optional<Boolean> isFailIfNotExists() {
    return Optional.ofNullable(failIfNotExists);
  }

  public void setFailIfNotExists(boolean failIfNotExists) {
    this.failIfNotExists = failIfNotExists;
  }

  public void setEnvironmentSpecificValues() throws DeploymentException {
    super.setEnvironmentSpecificValues();

    String isArmInsecure = getProperty("arm.insecure");
    if (isNotBlank(isArmInsecure)) {
      setArmInsecure(Boolean.valueOf(isArmInsecure));
    }
    if (!isArmInsecure().isPresent()) {
      setArmInsecure(false);
    }

    if (!isFailIfNotExists().isPresent()) {
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
  }
}
