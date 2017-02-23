/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.artifact.archiver.api;


import org.apache.commons.cli.*;
import org.mule.tools.artifact.archiver.internal.PackageBuilder;
import org.mule.tools.artifact.archiver.internal.packaging.PackagingTypeFactory;
import org.mule.tools.artifact.archiver.internal.packaging.type.PackagingType;

import java.io.File;
import java.io.IOException;

/**
 * Main class of the Mule Artifact Archiver project.
 */
public class MuleArtifactArchiverLauncher {
    private static final String ARTIFACT_CONTENT_TYPE = "artifactContent";
    private static final String ARTIFACT_CONTENT_TYPE_ARGUMENT_NAME = "content type";
    private static final String TARGET_FOLDER_ARGUMENT_NAME = "absolute path";
    private static final String TARGET_FOLDER = "target";
    private static final String PACKAGE_NAME = "packageName";
    private static final String PACKAGE_NAME_ARGUMENT_NAME = "name";

    private static final String USER_DIR = "user.dir";
    private static final String MULE_ARTIFACT_ARCHIVER_COMMAND_LINE_NAME = "muleArtifactArchiver";


    private static final HelpFormatter formatter = new HelpFormatter();
    private static final Option ARTIFACT_TYPE_OPTION = Option.builder().argName(ARTIFACT_CONTENT_TYPE_ARGUMENT_NAME).longOpt(ARTIFACT_CONTENT_TYPE).hasArg().desc("define if package contains binaries and/or sources").build();
    private static final Option TARGET_FOLDER_OPTION = Option.builder().argName(TARGET_FOLDER_ARGUMENT_NAME).longOpt(TARGET_FOLDER).hasArg().desc("path of source package structure root folder").build();
    private static final Option PACKAGE_NAME_OPTION = Option.builder().argName(PACKAGE_NAME_ARGUMENT_NAME).longOpt(PACKAGE_NAME).hasArg().desc("file name for generated file").required().build();
    private static final String HEADER = "Create package containing binaries and/or sources.\n\n";;
    private static final String FOOTER = "";
    private static Options options;
    private static PackageBuilder packageBuilder;

    public static void main(String[] args) throws IOException {
        createOptions();
        CommandLine cmd = parse(args);
        if(cmd != null) {
            String packagingType = cmd.getOptionValue(ARTIFACT_CONTENT_TYPE, PackagingTypeFactory.BINARIES_PACKAGING);
            PackageBuilder packageBuilder = getPackageBuilder(PackagingTypeFactory.getPackaging(packagingType));
            String targetFolder = cmd.getOptionValue(TARGET_FOLDER, System.getProperty(USER_DIR));
            String destinationFile = targetFolder + File.separator + cmd.getOptionValue(PACKAGE_NAME) + ".zip";
            packageBuilder.generateArtifact(new File(targetFolder), new File(destinationFile));
        }
    }

    private static CommandLine parse(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            printHelp();
        }
        return null;
    }

    private static void printHelp() {
        formatter.printHelp(MULE_ARTIFACT_ARCHIVER_COMMAND_LINE_NAME, HEADER, options, FOOTER, true);
    }

    private static void createOptions() {
        options = new Options();
        options.addOption(PACKAGE_NAME_OPTION);
        options.addOption(ARTIFACT_TYPE_OPTION);
        options.addOption(TARGET_FOLDER_OPTION);
    }

    public void setPackageBuilder(PackageBuilder packageBuilder) {
        this.packageBuilder = packageBuilder;
    }

    public static PackageBuilder getPackageBuilder(PackagingType packagingType) {
        if(packageBuilder == null) {
            packageBuilder = new PackageBuilder(packagingType);
        }
        return packageBuilder;
    }
}
