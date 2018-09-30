/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.api.classloader;

/**
 * Constants used for ClassLoaderModel attributes
 *
 * @since 3.2.0
 */
public class Constants {

  public static final String CLASSLOADER_MODEL_FILE_NAME = "classloader-model.json";

  public static final String SHARED_LIBRARIES_FIELD = "sharedLibraries";
  public static final String SHARED_LIBRARY_FIELD = "sharedLibrary";

  public static final String ADDITIONAL_PLUGIN_DEPENDENCIES_FIELD = "additionalPluginDependencies";
  public static final String PLUGIN_FIELD = "plugin";
  public static final String PLUGIN_DEPENDENCIES_FIELD = "dependencies";
  public static final String PLUGIN_DEPENDENCY_FIELD = "dependency";

  public static final String ARTIFACT_IS_SHARED_FIELD = "isShared";

}
