/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.maven.utils;

import org.apache.maven.plugin.logging.Log;
import org.mule.runtime.api.deployment.meta.MuleApplicationModel;
import org.mule.runtime.api.meta.MuleVersion;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Reads the mule application model and gives access to its properties based on the user settings
 * 
 * @since 2.0.0
 * @author Mulesoft Inc.
 */
public class MuleApplicationModelLoader {

  public static final String MULE_ARTIFACT_JSON_FILE_NAME = "mule-artifact.json";
  public static final MuleVersion MIN_MULE_GA_VERSION = new MuleVersion("4.1.1");

  private final MuleApplicationModel muleApplicationModel;
  private String runtimeVersion;
  private final Log log;

  public MuleApplicationModelLoader(MuleApplicationModel muleApplicationModel, Log log) {
    this.muleApplicationModel = muleApplicationModel;
    this.log = log;
  }

  public MuleApplicationModelLoader withRuntimeVersion(String runtimeVersion) {
    this.runtimeVersion = runtimeVersion;
    return this;
  }

  public String getRuntimeVersion() {
    if (isNotBlank(runtimeVersion)) {
      return runtimeVersion;
    }
    String runtimeVersion = getMinMuleVersion();
    log.debug("Runtime version set to " + runtimeVersion + " obtained from the " + MULE_ARTIFACT_JSON_FILE_NAME + " file");
    return runtimeVersion;
  }

  private String getMinMuleVersion() {
    MuleVersion minMuleVersion = new MuleVersion(muleApplicationModel.getMinMuleVersion());
    if (minMuleVersion.atLeast(MIN_MULE_GA_VERSION)) {
      return muleApplicationModel.getMinMuleVersion();
    }
    return MIN_MULE_GA_VERSION.toCompleteNumericVersion();
  }

}
