/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.weave.api;

import org.mule.runtime.core.api.util.IOUtils;
import org.mule.weave.v2.dwb.api.IWeaveValue;
import org.mule.weave.v2.dwb.api.WeaveProcessor;

import java.io.InputStream;
import java.util.Map;

/**
 * This class mocks the REDEFINES usage.
 */
public class DwbCustomProcessor implements WeaveProcessor {

    @Override
    public Object get(InputStream inputStream, String key, Map<String, IWeaveValue<?>> map) {
        final IWeaveValue<?> schema = map.get("schemaPath");
        if (schema == null) {
            throw new RuntimeException("SchemaPath not present");
        }
        final IWeaveValue<?> path = map.get("path");
        if (path == null) {
            throw new RuntimeException("path not present");
        }
        String message = IOUtils.toString(inputStream);
        int length = message.length();
        if ("raw".equals(key)) {
            return message.substring(1, length - 1);
        } else if ("sanitized".equals(key)) {
            return message.substring(3, length - 3);
        } else {
            return message;
        }
    }
}
