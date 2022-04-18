/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.api.classloader.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public class ArrayUtils {

  @SuppressWarnings("unchecked")
  public static <T> T[] copyOf(T[] original, Function<T, T> copyFunction) {
    checkNotNull(original, "original cannot be null");

    if (org.apache.commons.lang3.ArrayUtils.isEmpty(original) || Objects.isNull(copyFunction)) {
      return original;
    }

    return Arrays.stream(original)
        .map(copyFunction)
        .toArray(size -> (T[]) Array.newInstance(original.getClass().getComponentType(), size));
  }
}
