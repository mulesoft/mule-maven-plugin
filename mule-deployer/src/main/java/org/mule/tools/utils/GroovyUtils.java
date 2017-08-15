package org.mule.tools.utils;

import groovy.lang.GroovyShell;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.mule.tools.model.DeploymentConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class GroovyUtils {

  public static void executeScript(MavenProject project, DeploymentConfiguration configuration) throws MojoExecutionException {
    GroovyShell shell = new GroovyShell();
    shell.setProperty("basedir", project.getBasedir());

    for (Map.Entry entry : project.getProperties().entrySet()) {
      shell.setProperty((String) entry.getKey(), entry.getValue());
    }

    try {
      shell.evaluate(readFile(configuration.getScript().getAbsolutePath()));
    } catch (IOException e) {
      throw new MojoExecutionException("error executing script: " + configuration.getScript().getAbsolutePath() + "\n"
          + e.getMessage());
    }
  }

  private static String readFile(String file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line;
    StringBuilder stringBuilder = new StringBuilder();

    while ((line = reader.readLine()) != null) {
      stringBuilder.append(line);
      stringBuilder.append("\n");
    }

    return stringBuilder.toString();
  }
}
