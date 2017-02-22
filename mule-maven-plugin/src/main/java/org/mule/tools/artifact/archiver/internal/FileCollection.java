/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.artifact.archiver.internal;

import java.io.File;
import java.io.FileFilter;
import java.util.*;

// TODO why do we have this
public class FileCollection {

    private Set<File> files = new TreeSet<>(new FileComparator());
    private Set<File> folders = new TreeSet<>(new FileComparator());
    private Set<File> exclusions = new TreeSet<>(new FileComparator());
    private boolean returnDirectoriesOnly;

    public void addFile(File file) {
        files.add(file);
    }

    public void addFiles(File... files) {
        for (File file : files) {
            this.files.add(file);
        }
    }

    public void addFolder(File folder) {
        this.folders.add(folder);
    }

    public void addFolders(File... folders) {
        for (File folder : folders) {
            this.folders.add(folder);
        }
    }

    public Collection<File> allFiles() {
        Set<File> files = new TreeSet<File>(new FileComparator());
        if (returnDirectoriesOnly) {
            for (File folder : this.folders) {
                if (folder.exists() && !exclusions.contains(folder)) {
                    files.add(folder);
                }
            }
        } else {
            addFilesWithoutExclusions(this.files, files);
            for (File folder : folders) {
                if (folder.exists()) {
                    final List<File> folderFiles = Arrays.asList(folder.listFiles(new FileFilter() {

                        @Override
                        public boolean accept(File file) {
                            return file.isFile() && !file.isHidden() && file.exists();
                        }
                    }));
                    addFilesWithoutExclusions(folderFiles, files);
                }
            }
        }
        return files;
    }

    private void addFilesWithoutExclusions(Collection<File> filesToAdd, Set<File> files) {
        for (File file : filesToAdd) {
            if (!exclusions.contains(file)) {
                files.add(file);
            }
        }
    }

    public FileCollection returnDirectoriesOnly() {
        returnDirectoriesOnly = true;
        return this;
    }

    public void excludeFiles(File... files) {
        this.exclusions.addAll(Arrays.asList(files));
    }

    public Set<File> allExcludedFiles() {
        return exclusions;
    }
}
