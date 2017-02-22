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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.tools.artifact.archiver.internal.MuleArchiver;
import org.mule.tools.artifact.archiver.internal.packaging.PackageStructureValidator;
import org.mule.tools.artifact.archiver.internal.packaging.PackagingTypeFactory;
import org.mule.tools.artifact.archiver.internal.packaging.type.PackagingType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builder for Mule Application archives.
 */
public class PackageBuilder {

    public static final String POM = "pom.xml";
    public static final String MULE_APP_PROPERTIES = "mule-app.properties";
    public static final String MULE_DEPLOY_PROPERTIES = "mule-deploy.properties";

    public static final String CLASSES_FOLDER = "classes";
    public static final String MULE_FOLDER = "mule";
    public static final String METAINF_FOLDER = "META-INF";
    public static final String REPOSITORY_FOLDER = "repository";
    private static PackagingType packagingType;

    private File classesFolder = null;
    private File libFolder = null;
    private File muleFolder = null;
    private File metaInfFolder = null;
    private File pluginsFolder = null;
    private File repositoryFolder = null;

    private List<File> rootResources = new ArrayList<>();

    private File pomFile = null;
    private File muleDeployPropertiesFile = null;
    private File muleAppPropertiesFile = null;


    private File destinationFile;
    private MuleArchiver archiver = null;

    private transient Log log = LogFactory.getLog(this.getClass());
    private PackageStructureValidator applicationStructureValidator;

    public PackageBuilder(PackagingType packagingType) {
        this.packagingType = packagingType;
    }

    public PackageBuilder() {
        this(PackagingTypeFactory.getDefaultPackaging());
    }

    /**
     * @param archiver
     * @return builder
     */
    public PackageBuilder withArchiver(MuleArchiver archiver) {
        checkNotNull(archiver, "The org.mule.tools.artifact.org.mule.tools.artifact.archiver must not be null");
        this.archiver = archiver;
        return this;
    }

    /**
     * @param folder folder with all the configuration files of the application
     * @return builder
     */
    public PackageBuilder withClasses(File folder) {
        checkNotNull(folder, "The folder must not be null");
        classesFolder = folder;
        return this;
    }

    public PackageBuilder withMule(File folder) {
        checkNotNull(folder);
        muleFolder = folder;
        return this;
    }

    public PackageBuilder withMetaInf(File folder) {
        checkNotNull(folder, "The folder must not be null");
        metaInfFolder = folder;
        return this;
    }

    public PackageBuilder withRepository(File folder) {
        checkNotNull(folder, "The folder must not be null");
        repositoryFolder = folder;
        return this;
    }


    /**
     * @param file pom.xml file
     * @return builder
     */
    @Deprecated
    public PackageBuilder withPom(File file) {
        checkArgument(file.getName().equals(POM), "File must be named " + POM);
        pomFile = file;
        return this;
    }

    /**
     * @param file mule-deploy.properties application file
     * @return builder
     */
    @Deprecated
    public PackageBuilder withMuleDeployProperties(File file) {
        checkArgument(file.getName().equals(MULE_DEPLOY_PROPERTIES), "File must be named " + MULE_DEPLOY_PROPERTIES);
        muleDeployPropertiesFile = file;
        return this;
    }

    /**
     * @param file mule-app.properties application file
     * @return builder
     */
    @Deprecated
    public PackageBuilder withMuleAppProperties(File file) {
        checkArgument(file.getName().equals(MULE_APP_PROPERTIES), "File must be named " + MULE_APP_PROPERTIES);
        muleAppPropertiesFile = file;
        return this;
    }

    @Deprecated
    public PackageBuilder withLib(File folder) {
        checkNotNull(folder, "The folder must not be null");
        libFolder = folder;
        return this;
    }

    @Deprecated
    public PackageBuilder withPlugins(File folder) {
        checkNotNull(folder, "The folder must not be null");
        pluginsFolder = folder;
        return this;
    }

    public PackageBuilder withRootResource(File resource) {
        checkNotNull(resource, "The resource must not be null");
        rootResources.add(resource);
        return this;
    }

    /**
     * @param file file to be created with the content of the app
     * @return
     */
    public PackageBuilder withDestinationFile(File file) {
        checkNotNull(file, "The file must not be null");
        checkArgument(!file.exists(), "The file must not be duplicated");
        this.destinationFile = file;
        return this;
    }

    /**
     * Creates the application package.
     *
     * @throws IOException
     */
    public void createDeployableFile() throws IOException {
        runPrePackageValidations();

        MuleArchiver archiver = getMuleArchiver();
        if (null != muleFolder && muleFolder.exists() && muleFolder.isDirectory()) {
            archiver.addMule(muleFolder, null, null);
        }

        if (null != classesFolder && classesFolder.exists() && classesFolder.isDirectory()) {
            archiver.addClasses(classesFolder, null, null);
            // Warning
        }

        if (null != metaInfFolder && metaInfFolder.exists() && metaInfFolder.isDirectory()) {
            archiver.addMetaInf(metaInfFolder, null, null);
            // Warning
        }

        if (null != repositoryFolder && repositoryFolder.exists() && repositoryFolder.isDirectory()) {
            archiver.addRepository(repositoryFolder, null, null);
            // Warning
        }

        archiver.setDestFile(destinationFile);
        archiver.createArchive();
    }

    public void setApplicationStructureValidator(PackageStructureValidator applicationStructureValidator) {
        this.applicationStructureValidator = applicationStructureValidator;
    }


    private void runPrePackageValidations() {
        checkNotNull(destinationFile, "The destination file has not been set");
    }

    private void checkMandatoryFolder(File folder) {
        checkNotNull(folder, "The folder must not be null");
        checkArgument(folder.exists(), "The folder must exists");
        checkArgument(folder.isDirectory(), "The folder must be a valid directory");
    }

    //****************************************************************************************************************************
    // TODO check this scenario



    /**
     * Resource go under: app-folder/
     *
     * @param file file to be included in the root folder of the app
     * @return builder
     */
    public PackageBuilder addRootResourcesFile(File file) {
        //        this.rootResourceFolder.addFile(file);
        return this;
    }

    public MuleArchiver getMuleArchiver() {
        if(archiver == null) {
            archiver = new MuleArchiver();
        }
        return archiver;
    }

    public void generateArtifact(File targetFolder, File destinationFile) throws IOException {
        checkMandatoryFolder(targetFolder);
        checkNotNull(destinationFile);
        checkArgument(destinationFile != null && !destinationFile.exists(), "Destination file must not be null or already exist");
        File[] files = targetFolder.listFiles();
        if(files == null) {
            log.warn("The provided target folder is empty, no file will be generated");
            return;
        }
        if(getApplicationPackageStructureValidator().hasExpectedStructure(files)) {
            Map<String,File> fileMap = Arrays.stream(files).collect(Collectors.toMap(File::getName, Function.identity()));
            this.packagingType.applyPackaging(this, fileMap).withDestinationFile(destinationFile);
            this.createDeployableFile();
            log.info("File " + destinationFile.getName() + " has been successfully created");
        } else {
            log.warn("The provided target folder does not have the expected structure");
        }
    }

    public PackageStructureValidator getApplicationPackageStructureValidator() {
        return this.applicationStructureValidator != null ? this.applicationStructureValidator : new PackageStructureValidator(this.packagingType);
    }


    //****************************************************************************************************************************
    // TODO why?
    //    private static final String MULE_CONFIG_XML = "mule-config.xml";
    //    private final Map<String, FileCollection> extraResources = new HashMap<>();
    //    private final FileCollection jarFolders = new FileCollection();
    //    private final FileCollection rootResourceFolder = new FileCollection();
    //    private final FileCollection resourceFolders = new FileCollection().returnDirectoriesOnly();
    //    private final FileCollection classesFolders = new FileCollection().returnDirectoriesOnly();

    //    private void buildResourceFolder(MuleArchiver muleArchiver) {
    //        for (File rootResourceFile : rootResourceFolder.allFiles()) {
    //            if (rootResourceFile.isFile()) {
    //                muleArchiver.addRootFile(rootResourceFile);
    //            } else {
    //                muleArchiver.addRootDirectory(rootResourceFile);
    //            }
    //        }
    //        //               TODO why do we have this?
    //        //        for (Map.Entry<String, FileCollection> extraFiles : this.extraResources.entrySet()) {
    //        //            for (File resourceFile : extraFiles.getValue().allFiles()) {
    //        //                if (resourceFile.isFile()) {
    //        //                    muleArchiver.addResourcesFileToPath(resourceFile, extraFiles.getKey());
    //        //                } else {
    //        //                    muleArchiver.addResourcesToPath(resourceFile, extraFiles.getKey());
    //        //                }
    //        //            }
    //        //        }
    //    }

    //    private void buildClassesFolder(MuleArchiver muleArchiver) {
    //        for (File classesFolder : classesFolders.allFiles()) {
    //            muleArchiver.addClasses(classesFolder, null, transformToPatterns(resourceFolders.allExcludedFiles()));
    //        }
    //
    //        for (File resourceFolder : resourceFolders.allFiles()) {
    //            muleArchiver.addClasses(resourceFolder, null, transformToPatterns(resourceFolders.allExcludedFiles()));
    //        }
    //    }
    //    private void buildLibFolder(MuleArchiver muleArchiver) {
    //        jarFolders.allFiles().forEach(f -> muleArchiver.addLib(f));
    //    }
    //    private String[] transformToPatterns(Set<File> files) {
    //        final String[] patterns = new String[files.size()];
    //        int i = 0;
    //        for (File file : files) {
    //            patterns[i] = "**/" + file.getName();
    //            i++;
    //        }
    //        return patterns;
    //    }
    //    public MuleApplicationArchiveBuilder excludeFromClassesFolder(File... files) {
    //        this.resourceFolders.excludeFiles(files);
    //        return this;
    //    }

    //    private String sanitizePath(String path) {
    //        if (path == null) {
    //            throw new IllegalArgumentException("path can not be null");
    //        } else if (path.isEmpty()) {
    //            return path;
    //        } else if (!path.endsWith("/")) {
    //            return path + "/";
    //        }
    //        return path;
    //    }
    //    /**
    //     * Resource go under: app-folder/classes/
    //     *
    //     * @param folder folder content to be included in the classes folder of the app
    //     * @return
    //     */
    //    public MuleApplicationArchiveBuilder addResourcesFolder(File folder) {
    //        this.resourceFolders.addFolder(folder);
    //        return this;
    //    }
    //    /**
    //     * Resource go under: app-folder/classes/
    //     *
    //     * @param folders folders content to be included in the classes folder of the app
    //     * @return
    //     */
    //    public MuleApplicationArchiveBuilder addResourcesFolders(File... folders) {
    //        this.resourceFolders.addFolders(folders);
    //        return this;
    //    }
    //
    //    public MuleApplicationArchiveBuilder addClassesFolder(File folder) {
    //        this.classesFolders.addFolder(folder);
    //        return this;
    //    }
    //
    //    public MuleApplicationArchiveBuilder addClassesFolders(File... folders) {
    //        this.classesFolders.addFolders(folders);
    //        return this;
    //    }
    //    public MuleApplicationArchiveBuilder addJarLibrary(File jarFile) {
    //        this.jarFolders.addFile(jarFile);
    //        return this;
    //    }
    //    public MuleApplicationArchiveBuilder addJarLibraries(File... jarFiles) {
    //        this.jarFolders.addFiles(jarFiles);
    //        return this;
    //    }
    //    public MuleApplicationArchiveBuilder addJarLibraryFolder(File folder) {
    //        this.jarFolders.addFolder(folder);
    //        return this;
    //    }

    // TODO validate the actual need for this
    //    /**
    //     * @param folder folder to include in the <code>path</code> location.
    //     * @param path   path where the <code>folder</code> should be included.
    //     * @return builder
    //     */
    //    public MuleApplicationArchiveBuilder addExtraResourceFolder(File folder, String path) {
    //        FileCollection pathCollection;
    //        path = this.sanitizePath(path);
    //        if (this.extraResources.containsKey(path)) {
    //            pathCollection = this.extraResources.get(path);
    //        } else {
    //            pathCollection = new FileCollection();
    //        }
    //        pathCollection.addFolder(folder);
    //        this.extraResources.put(path, pathCollection);
    //        return this;
    //    }
    //
    //    /**
    //     * @param file file to include in the <code>path</code> location.
    //     * @param path path where the <code>file</code> should be included.
    //     * @return builder
    //     */
    //    public MuleApplicationArchiveBuilder addExtraResourceFile(File file, String path) {
    //        FileCollection pathCollection;
    //        path = sanitizePath(path);
    //        if (this.extraResources.containsKey(path)) {
    //            pathCollection = this.extraResources.get(path);
    //        } else {
    //            pathCollection = new FileCollection();
    //        }
    //        pathCollection.addFile(file);
    //        this.extraResources.put(path, pathCollection);
    //        return this;
    //    }


    //    /**
    //     * @param muleConfigFile mule-config.xml configuration file
    //     * @return builder
    //     */
    //    public MuleApplicationArchiveBuilder setMuleConfigFile(File muleConfigFile) {
    //        if (!muleConfigFile.getName().equals(MULE_CONFIG_XML)) {
    //            throw new IllegalArgumentException("File must be named " + MULE_CONFIG_XML);
    //        }
    //        this.rootResourceFolder.addFile(muleConfigFile);
    //        return this;
    //    }
}
