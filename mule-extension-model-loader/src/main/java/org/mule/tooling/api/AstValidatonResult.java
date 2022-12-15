/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.api;

import org.mule.runtime.ast.api.validation.ValidationResultItem;

import java.util.List;

public class AstValidatonResult {

  private final List<ValidationResultItem> errors;
  private final List<ValidationResultItem> warnings;
  private final List<ValidationResultItem> dynamicStructureErrors;

  public AstValidatonResult(List<ValidationResultItem> errors, List<ValidationResultItem> warnings,
                            List<ValidationResultItem> dynamicStructureErrors) {
    this.errors = errors;
    this.warnings = warnings;
    this.dynamicStructureErrors = dynamicStructureErrors;
  }

  public List<ValidationResultItem> getErrors() {
    return errors;
  }

  public List<ValidationResultItem> getWarnings() {
    return warnings;
  }

  public List<ValidationResultItem> getDynamicStructureErrors() {
    return dynamicStructureErrors;
  }
}
