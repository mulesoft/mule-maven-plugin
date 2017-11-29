/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package integration;

import java.io.File;
import java.util.*;

import org.apache.maven.shared.utils.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class FileTreeMatcher {

  private static String PADDING = "    ";
  public static final String HORIZONTAL_LINE = StringUtils.repeat("-", PADDING.length() - 1);

  public static Matcher<File> hasSameTreeStructure(final File root, String[] excludes) {
    return new BaseMatcher<File>() {

      @Override
      public boolean matches(Object item) {
        if (root == null) {
          throw new IllegalArgumentException("Do not use this matcher to compare against null");
        }
        if (item == null) {
          return false;
        }
        final File otherRoot = (File) item;
        return root.isDirectory() && otherRoot.isDirectory() && sameTreeSructure(root, otherRoot);
      }

      @Override
      public void describeTo(Description description) {
        String treeRepresentation = generateTreeRepresentation(root, PADDING.length()).toString();
        description.appendText("hasSameTreeStructure was expecting the following directory structure: \n\n")
            .appendText(StringUtils.isEmpty(treeRepresentation) ? PADDING + "<empty>\n" : treeRepresentation);
      }

      @Override
      public void describeMismatch(final Object item, final Description description) {
        String treeRepresentation = item != null ? generateTreeRepresentation((File) item, PADDING.length()).toString() : "null";
        description.appendText("was \n\n")
            .appendText(StringUtils.isEmpty(treeRepresentation) ? PADDING + "<empty>\n" : treeRepresentation);
      }

      private StringBuilder generateTreeRepresentation(File root, int padding) {
        StringBuilder treeRepresentation = new StringBuilder("");
        if (root.isFile()) {
          return treeRepresentation;
        }
        File[] rootChildren = root.listFiles() == null ? null
            : Arrays.stream(root.listFiles()).filter(file -> !Arrays.asList(excludes).contains(file.getName()))
                .toArray(File[]::new);
        for (File child : rootChildren) {
          treeRepresentation.append(generateLeftPadding(padding) + child.getName() + ((child.isDirectory()) ? "/\n" : "\n"));
          treeRepresentation.append(generateTreeRepresentation(child, padding + PADDING.length()));
        }
        return treeRepresentation;
      }

      private String generateLeftPadding(int padding) {
        if (padding == PADDING.length()) { // element in root directory
          return PADDING;
        }
        return StringUtils.repeat(" ", padding - PADDING.length()) + "\u2514" + HORIZONTAL_LINE;

      }

      private boolean sameTreeSructure(File root, File otherRoot) {
        File[] rootChildrenArray = root.listFiles() == null ? null
            : Arrays.stream(root.listFiles()).filter(file -> !Arrays.asList(excludes).contains(file.getName()))
                .toArray(File[]::new);
        File[] otherRootChildrenArray = otherRoot.listFiles() == null ? null
            : Arrays.stream(otherRoot.listFiles()).filter(file -> !Arrays.asList(excludes).contains(file.getName()))
                .toArray(File[]::new);

        if (rootChildrenArray == null || otherRootChildrenArray == null || rootChildrenArray.length == 0
            || otherRootChildrenArray.length == 0) {
          return !((rootChildrenArray == null || rootChildrenArray.length == 0)
              ^ (otherRootChildrenArray == null || otherRootChildrenArray.length == 0));
        }
        Set<File> rootChildren = new TreeSet<>(Arrays.asList(rootChildrenArray));
        Set<File> otherRootChildren = new TreeSet<>(Arrays.asList(otherRootChildrenArray));
        if (rootChildren.size() != otherRootChildren.size()) {
          return false;
        }
        Iterator<File> rootChildrenIterator = rootChildren.iterator();
        Iterator<File> otherRootChildrenIterator = otherRootChildren.iterator();
        while (rootChildrenIterator.hasNext() && otherRootChildrenIterator.hasNext()) {
          File rootChild = rootChildrenIterator.next();
          File otherRootChild = otherRootChildrenIterator.next();
          if (!areSameFile(rootChild, otherRootChild) || !sameTreeSructure(rootChild, otherRootChild)) {
            return false;
          }
        }
        return true;
      }

      private boolean areSameFile(File file, File otherFile) {
        return StringUtils.equals(file.getName(), otherFile.getName()) && (file.isFile() == otherFile.isFile());
      }


    };
  }

  @Override
  public String toString() {
    return "FileTreeMatcher";
  }

}

