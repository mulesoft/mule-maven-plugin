/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package integration.test.util.rules.environment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.fail;

public class AgentEnvironment extends StandaloneEnvironment {

  private static final String UNENCRYPTED_CONNECTION_OPTION = "-I";

  public AgentEnvironment(String muleVersion) {
    super(muleVersion);
  }

  @Override
  protected void start() throws IOException, InterruptedException, TimeoutException {
    unpackAgent();
    super.start();
  }

  private void unpackAgent() throws InterruptedException, IOException {
    List<String> commands = new ArrayList<>();

    String amcExecutable = muleHome.getRoot().getAbsolutePath() + AMC_SETUP_RELATIVE_FOLDER;

    int tries = 0;
    Process applicationProcess;
    do {
      if (tries != 0) {
        log.info("Failed to unpack agent. Trying to unpack again...");
      }
      commands.clear();
      commands.add(amcExecutable);
      commands.add(UNENCRYPTED_CONNECTION_OPTION);
      log.info("Unpacking agent...");
      applicationProcess = Runtime.getRuntime().exec(commands.toArray(new String[0]));
      applicationProcess.waitFor();
      tries++;
      if (tries == UNPACK_AGENT_MAX_ATTEMPTS) {
        fail("Could not unpack agent");
      }
    } while (applicationProcess.exitValue() != NORMAL_TERMINATION);
    log.info("Agent successfully unpacked.");
  }
}
