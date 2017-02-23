/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.mojo;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.util.IOUtil;

import java.io.*;
import java.util.Set;


@Mojo(name = "install",
    defaultPhase = LifecyclePhase.INSTALL,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
@Deprecated
public class InstallMojo extends AbstractMuleMojo {

  /**
   * If set to <code>true</code> attempt to copy the domain of this Mule application to mule.home/domains or $MULE_HOME/domains
   */
  // TODO review this and disable till domain is supported
  @Parameter(property = "installDomain", required = true)
  protected boolean installDomain;

  /**
   * If the mule app belongs to a domain you can set this value with the domain dependency information groupId:artifactId:version so during
   * install phase the mule domain maven plugin can deploy the domain before deploying the application.
   */
  // TODO review this and disable till domain is supported
  @Parameter(property = "domainDependency", required = true)
  protected String domainDependency;

  /**
   * If set to <code>true</code> attempt to copy the Mule application zip to mule.home/apps or $MULE_HOME/apps
   */
  // TODO review this and disable till domain is supported
  @Parameter(property = "copyToAppsDirectory", required = true)
  protected boolean copyToAppsDirectory;

  public void execute() throws MojoExecutionException, MojoFailureException {
    if (copyToAppsDirectory) {
      File muleHome = determineMuleHome();
      if (muleHome != null) {
        if (installDomain) {
          if (domainDependency == null || domainDependency.trim().equals("") || domainDependency.equals("empty")) {
            throw new MojoExecutionException(
                                             "You must configure the domainDependency configuration attribute and specify the domain dependency in order to install the domain in the mule server");
          }
          String[] split = domainDependency.split(":");
          if (split.length != 3) {
            throw new MojoExecutionException(
                                             "domainDependency attribute does not declare groupId or artifactId or version");
          }
          String domainGroupId = split[0];
          String domainArtifactId = split[1];
          String domainVersion = split[2];

          Set dependencyArtifacts = project.getDependencyArtifacts();
          boolean domainFound = false;
          for (Object dependencyArtifact : dependencyArtifacts) {
            Artifact artifact = (Artifact) dependencyArtifact;
            if (artifact.getGroupId().equals(domainGroupId) && artifact.getArtifactId().equals(domainArtifactId)
                && artifact.getVersion().equals(domainVersion)) {
              domainFound = true;
              try {
                copyZipFileToDestDirUsingTempFile(artifact.getFile(), new File(muleHome, "domains"));
              } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
              }
              break;
            }
          }
          if (!domainFound) {
            throw new MojoExecutionException(
                                             "installDomain was configured but domain dependency is not available in the project. Did you forgot to add the domain as a dependency with type zip?");
          }
        }
        copyMuleAppZipToMuleHome(muleHome);
      } else {
        getLog().warn("MULE_HOME is not set, not copying " + finalName + ".zip");
      }
    }
  }

  private File determineMuleHome() throws MojoExecutionException {
    File muleHomeFile = null;

    String muleHome = getMuleHomeEnvVarOrSystemProperty();
    if (muleHome != null) {
      muleHomeFile = new File(muleHome);
      if (muleHomeFile.exists() == false) {
        String message =
            String.format("MULE_HOME is set to %1s but this directory does not exist.",
                          muleHome);
        throw new MojoExecutionException(message);
      }
      if (muleHomeFile.canWrite() == false) {
        String message =
            String.format("MULE_HOME is set to %1s but the directory is not writeable.",
                          muleHome);
        throw new MojoExecutionException(message);
      }
    }

    return muleHomeFile;
  }

  private String getMuleHomeEnvVarOrSystemProperty() {
    String muleHome = System.getProperty("mule.home");
    if (muleHome == null) {
      // fall back to enviroment property $MULE_HOME
      muleHome = System.getenv("MULE_HOME");
    }
    return muleHome;
  }

  // TODO why!!!!!!!!!
  private void copyMuleAppZipToMuleHome(File muleHome) throws MojoExecutionException {
    try {
      copyZipFileToDestDirUsingTempFile(getMuleAppZipFile(), muleAppsDirectory(muleHome));
    } catch (IOException iox) {
      throw new MojoExecutionException("Exception while copying to apps directory", iox);
    }
  }

  private void copyZipFileToDestDirUsingTempFile(File zipFile, File dest) throws IOException, MojoExecutionException {
    if (!dest.isDirectory()) {
      throw new IllegalArgumentException("destination must be a directory");
    }
    InputStream muleZipInput = null;
    OutputStream tempOutput = null;
    try {
      muleZipInput = new FileInputStream(zipFile);

      File tempFile = new File(dest, zipFile.getName().replace(".zip", ".temp"));
      tempOutput = new FileOutputStream(tempFile);

      IOUtil.copy(muleZipInput, tempOutput);

      getLog().info(String.format("Copying %1s to %2s", zipFile.getAbsolutePath(),
                                  tempFile.getAbsolutePath()));

      File finalFile = new File(dest, zipFile.getName());
      if (tempFile.renameTo(finalFile) == false) {
        throw new MojoExecutionException(String.format("Could not rename %1s to %2s",
                                                       tempFile.getAbsolutePath(), finalFile.getAbsolutePath()));
      }
    } finally {
      IOUtil.close(muleZipInput);
      IOUtil.close(tempOutput);
    }
  }

  private File muleAppsDirectory(File muleHome) {
    return new File(muleHome, "apps");
  }
}
