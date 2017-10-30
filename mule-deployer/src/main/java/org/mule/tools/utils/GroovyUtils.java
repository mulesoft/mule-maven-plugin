/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tools.utils;

import groovy.lang.GroovyShell;
import groovy.util.ScriptException;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class GroovyUtils {

  public static void executeScript(MavenProject project, File script) throws ScriptException {
    GroovyShell shell = new GroovyShell();
    shell.setProperty("basedir", project.getBasedir());

    for (Map.Entry entry : project.getProperties().entrySet()) {
      shell.setProperty((String) entry.getKey(), entry.getValue());
    }

    try {
      shell.evaluate(readFile(script.getAbsolutePath()));
    } catch (IOException e) {
      throw new ScriptException("error executing script: " + script.getAbsolutePath() + "\n"
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
