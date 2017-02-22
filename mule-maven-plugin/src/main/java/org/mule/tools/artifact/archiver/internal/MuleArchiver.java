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

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.codehaus.plexus.util.DirectoryScanner;

import java.io.File;

/**
 * Creates the structure and archive for a Mule Application
 */
public class MuleArchiver extends ZipArchiver {

    public final static String ROOT_LOCATION = "";

    public final static String PLUGINS_LOCATION = "plugins" + File.separator;

    public final static String LIB_LOCATION = "lib" + File.separator;

    public final static String CLASSES_LOCATION = "classes" + File.separator;

    public final static String MULE_LOCATION = "mule" + File.separator;

    public final static String METAINF_LOCATION = "META-INF" + File.separator;

    public static final String REPOSITORY_LOCATION = "repository" + File.separator;


    //    public final static String LIB_SHARED_LOCATION = LIB_LOCATION + "shared" + File.separator;
    //    public final static String CLASSES_PLUGIN_LOCATION = CLASSES_LOCATION + "plugin" + File.separator;
    //    public final static String METAINF_MULESRC_LOCATION = METAINF_LOCATION + "mule-src" + File.separator;


    public void addClasses(File file) throws ArchiverException {
        addFile(file, CLASSES_LOCATION + file.getName());
    }

    public void addClasses(File directoryName, String[] includes, String[] excludes) throws ArchiverException {
        addDirectory(directoryName, CLASSES_LOCATION, includes, addDefaultExcludes(excludes));
    }

    public void addMetaInf(File file) throws ArchiverException {
        addFile(file, METAINF_LOCATION + file.getName());
    }

    public void addMetaInf(File directoryName, String[] includes, String[] excludes) throws ArchiverException {
        addDirectory(directoryName, METAINF_LOCATION, includes, addDefaultExcludes(excludes));
    }

    public void addRepository(File file) throws ArchiverException {
        addFile(file, REPOSITORY_LOCATION + file.getName());
    }

    public void addRepository(File directoryName, String[] includes, String[] excludes) throws ArchiverException {
        addDirectory(directoryName, REPOSITORY_LOCATION, includes, addDefaultExcludes(excludes));
    }

    public void addMule(File file) throws ArchiverException {
        addFile(file, MULE_LOCATION + file.getName());
    }

    public void addMule(File directoryName, String[] includes, String[] excludes) throws ArchiverException {
        addDirectory(directoryName, MULE_LOCATION, includes, addDefaultExcludes(excludes));
    }

    public void addRootDirectory(File directory) throws ArchiverException {
        addDirectory(directory, ROOT_LOCATION, null, addDefaultExcludes(null));
    }

    @Deprecated
    public void addLib(File file) throws ArchiverException {
        addFile(file, LIB_LOCATION + file.getName());
    }

    @Deprecated
    public void addLib(File directoryName, String[] includes, String[] excludes) throws ArchiverException {
        addDirectory(directoryName, LIB_LOCATION, includes, addDefaultExcludes(excludes));
    }

    @Deprecated
    public void addPlugins(File file) throws ArchiverException {
        addFile(file, PLUGINS_LOCATION + file.getName());
    }

    @Deprecated
    public void addPlugins(File directoryName, String[] includes, String[] excludes) throws ArchiverException {
        addDirectory(directoryName, PLUGINS_LOCATION, includes, addDefaultExcludes(excludes));
    }

    @Deprecated
    public void addRootFile(File file) throws ArchiverException {
        addFile(file, ROOT_LOCATION + file.getName());
    }

    private String[] addDefaultExcludes(String[] excludes) {
        if ((excludes == null) || (excludes.length == 0)) {
            return DirectoryScanner.DEFAULTEXCLUDES;
        } else {
            String[] newExcludes = new String[excludes.length + DirectoryScanner.DEFAULTEXCLUDES.length];

            System.arraycopy(DirectoryScanner.DEFAULTEXCLUDES, 0, newExcludes, 0, DirectoryScanner.DEFAULTEXCLUDES.length);
            System.arraycopy(excludes, 0, newExcludes, DirectoryScanner.DEFAULTEXCLUDES.length, excludes.length);

            return newExcludes;
        }
    }
}
