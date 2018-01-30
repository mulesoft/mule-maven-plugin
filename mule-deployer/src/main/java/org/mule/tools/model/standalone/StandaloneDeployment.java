/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.model.standalone;

import org.apache.maven.plugins.annotations.Parameter;
import org.mule.tools.api.classloader.model.ArtifactCoordinates;
import org.mule.tools.client.core.exception.DeploymentException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StandaloneDeployment extends MuleRuntimeDeployment {

  @Parameter(readonly = true)
  protected ArtifactCoordinates muleDistribution;

  @Parameter
  protected File muleHome;

  @Deprecated
  @Parameter
  protected Integer timeout;

  @Parameter
  protected File domain;

  @Parameter
  protected String[] arguments;

  @Parameter
  @Deprecated
  private File script;

  @Parameter
  @Deprecated
  private List<ArtifactCoordinates> artifactItems = new ArrayList<>();

  /**
   * Maven coordinates for the Mule Runtime distribution to download. You need to specify:
   * <li>groupId</li>
   * <li>artifactId</li>
   * <li>version</li> This parameter and <code>muleVersion</code> are mutual exclusive
   *
   * @since 1.0
   * @deprecated Use the official maven artifact descriptor, if you need to use Community distribution @see community property
   */
  public ArtifactCoordinates getMuleDistribution() {
    return muleDistribution;
  }

  /**
   * Maven coordinates for the Mule Runtime distribution to download. You need to specify:
   * <li>groupId</li>
   * <li>artifactId</li>
   * <li>version</li> This parameter and <code>muleVersion</code> are mutual exclusive
   *
   * @since 1.0
   * @deprecated Use the official maven artifact descriptor, if you need to use Community distribution @see community property
   */
  public void setMuleDistribution(ArtifactCoordinates muleDistribution) {
    this.muleDistribution = muleDistribution;
  }

  /**
   * Path to a Mule Standalone server.
   */
  public File getMuleHome() {
    return muleHome;
  }

  /**
   * Set path to a Mule Standalone server.
   */
  public void setMuleHome(File muleHome) {
    this.muleHome = muleHome;
  }

  @Deprecated
  public Integer getTimeout() {
    return timeout;
  }

  @Deprecated
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public Optional<File> getDomain() {
    return Optional.ofNullable(domain);
  }

  public void setDomain(File domain) {
    this.domain = domain;
  }

  /**
   * List of Mule Runtime Standalone command line arguments. Adding a property to this list is the same that adding it to the
   * command line when starting Mule using bin/mule. If you want to add a Mule property don't forget to prepend <code>-M-D</code>.
   * If you want to add a System property for the Wrapper don't forget to prepend <code>-D</code>.
   * <p>
   * Example:
   * <code>&lt;arguments&gt;&lt;argument&gt;-M-Djdbc.url=jdbc:oracle:thin:@myhost:1521:orcl&lt;/argument&gt;&lt;/arguments&gt;</code>
   *
   * @since 1.0
   */
  public String[] getArguments() {
    if (arguments == null) {
      arguments = new String[0];
    }
    return arguments;
  }

  public void setArguments(String[] arguments) {
    this.arguments = arguments;
  }

  public void setEnvironmentSpecificValues() throws DeploymentException {
    super.setEnvironmentSpecificValues();
  }

  public File getScript() {
    return script;
  }

  public void setScript(File script) {
    this.script = script;
  }

  @Deprecated
  public List<ArtifactCoordinates> getArtifactItems() {
    return artifactItems;
  }

  @Deprecated
  public void setArtifactItems(List<ArtifactCoordinates> artifactItems) {
    this.artifactItems = artifactItems;
  }

}
