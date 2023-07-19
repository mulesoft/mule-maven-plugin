/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 */
package org.mule.tools.api.verifier;

import org.mule.tools.api.exception.ValidationException;

/**
 * Verifies that the packaging was done correctly
 */
public interface ProjectVerifier {

  void verify() throws ValidationException;

}
