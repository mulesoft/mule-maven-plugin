/**
 * Mule ESB Maven Tools
 *
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.maven.plugin.app;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.mule.tools.artifact.archiver.api.MuleApplicationArchiveBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;

/**
 * Build a Mule application archive.
 *
 * @phase package
 * @goal mule
 * @requiresDependencyResolution runtime
 */
public class MuleMojo extends AbstractMuleMojo
{
    public final static String LIB_LOCATION = "lib" + File.separator;

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    /**
     * Directory containing the classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;

    /**
     * Whether a JAR file will be created for the classes in the app. Using this optional
     * configuration parameter will make the generated classes to be archived into a jar file
     * and the classes directory will then be excluded from the app.
     *
     * @parameter expression="${archiveClasses}" default-value="false"
     */
    private boolean archiveClasses;

    /**
     * List of exclusion elements (having groupId and artifactId children) to exclude from the
     * application archive.
     *
     * @parameter
     * @since 1.2
     */
    private List<Exclusion> exclusions;

    /**
     * List of inclusion elements (having groupId and artifactId children) to exclude from the
     * application archive.
     *
     * @parameter
     * @since 1.5
     */
    private List<Inclusion> inclusions;

    /**
     * Exclude all artifacts with Mule groupIds. Default is <code>true</code>.
     *
     * @parameter default-value="true"
     * @since 1.4
     */
    private boolean excludeMuleDependencies;

    /**
     * @parameter default-value="false"
     * @since 1.7
     */
    private boolean filterAppDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        File app = getMuleAppZipFile();
        try
        {
            createMuleApp(app);
        }
        catch (ArchiverException e)
        {
            throw new MojoExecutionException("Exception creating the Mule App", e);
        }

        this.projectHelper.attachArtifact(this.project, "zip", app);
    }

    protected void createMuleApp(final File app) throws MojoExecutionException, ArchiverException
    {
        validateProject();

        final MuleApplicationArchiveBuilder muleApplicationArchiveBuilder = new MuleApplicationArchiveBuilder();

        try
        {
            addAppDirectory(muleApplicationArchiveBuilder);
            addCompiledClasses(muleApplicationArchiveBuilder);
            addApiFiles(muleApplicationArchiveBuilder);
            addDependencies(muleApplicationArchiveBuilder);
            addMappingsDirectory(muleApplicationArchiveBuilder);
            muleApplicationArchiveBuilder.setDestinationFile(app);

            app.delete();
            muleApplicationArchiveBuilder.createDeployableFile();
        }
        catch (IOException e)
        {
            getLog().error("Cannot create archive", e);
        }
    }

    private void addApiFiles(MuleApplicationArchiveBuilder muleApplicationArchiveBuilder)
    {
        if (this.apiDirectory.exists())
        {
            getLog().info("Copying api directly");
            addRecursively(this.apiDirectory, muleApplicationArchiveBuilder, "api");
        }
        else
        {
            getLog().info(this.apiDirectory + " does not exist, skipping");
        }
    }

    private void addRecursively(File directory, MuleApplicationArchiveBuilder muleApplicationArchiveBuilder, String path)
    {
        muleApplicationArchiveBuilder.addExtraResourceFolder(directory, path);

        if(directory != null && directory.listFiles() != null)
        {
            for(File file : directory.listFiles())
            {
                if(file.isDirectory())
                {
                    addRecursively(file, muleApplicationArchiveBuilder, path + "/" + file.getName());
                }
            }
        }
    }

    private void validateProject() throws MojoExecutionException
    {
        // TODO why we check for mule-config.xml?
        File muleConfig = new File(appDirectory, "mule-config.xml");
        File deploymentDescriptor = new File(appDirectory, "mule-deploy.properties");

        if (!muleConfig.exists() && !deploymentDescriptor.exists())
        {
            String message = String.format("No mule-config.xml or mule-deploy.properties in %1s",
                this.project.getBasedir());

            getLog().error(message);
            throw new MojoExecutionException(message);
        }
    }

    private void addAppDirectory(final MuleApplicationArchiveBuilder muleApplicationArchiveBuilder) throws ArchiverException, IOException
    {
        FileVisitor excludeFromClassesFolderFileVisitor = new FileVisitor<Path>()
        {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
            {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                muleApplicationArchiveBuilder.excludeFromClassesFolder(file.toFile());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException
            {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
            {
                muleApplicationArchiveBuilder.excludeFromClassesFolder(dir.toFile());
                return FileVisitResult.CONTINUE;
            }
        };

        if (filterAppDirectory)
        {
            muleApplicationArchiveBuilder.addRootResourcesFile(getFilteredAppDirectory());
            Files.walkFileTree(getFilteredAppDirectory().toPath(), excludeFromClassesFolderFileVisitor);
        }
        else
        {
            muleApplicationArchiveBuilder.addRootResourcesFile(appDirectory);
            Files.walkFileTree(appDirectory.toPath(), excludeFromClassesFolderFileVisitor);
        }
    }

    private void addCompiledClasses(MuleApplicationArchiveBuilder muleApplicationArchiveBuilder) throws ArchiverException, MojoExecutionException
    {
        if (!this.archiveClasses)
        {
            addClassesFolder(muleApplicationArchiveBuilder);
        }
        else
        {
            addArchivedClasses(muleApplicationArchiveBuilder);
        }
    }

    private void addClassesFolder(MuleApplicationArchiveBuilder muleApplicationArchiveBuilder) throws ArchiverException
    {
        if (this.classesDirectory.exists())
        {
            getLog().info("Copying classes directly");
            muleApplicationArchiveBuilder.addClassesFolder(this.classesDirectory);
        }
        else
        {
            getLog().info(this.classesDirectory + " does not exist, skipping");
        }
    }

    private void addMappingsDirectory(MuleApplicationArchiveBuilder muleApplicationArchiveBuilder) throws ArchiverException
    {
        if (this.mappingsDirectory.exists())
        {
            getLog().info("Copying mappings");
            muleApplicationArchiveBuilder.addResourcesFolder(this.mappingsDirectory);
        }
        else
        {
            getLog().info(this.mappingsDirectory + " does not exist, skipping");
        }
    }

    private void addArchivedClasses(MuleApplicationArchiveBuilder muleApplicationArchiveBuilder) throws ArchiverException, MojoExecutionException
    {
        if (!this.classesDirectory.exists())
        {
            getLog().info(this.classesDirectory + " does not exist, skipping");
            return;
        }

        getLog().info("Copying classes as a jar");

        final JarArchiver jarArchiver = new JarArchiver();
        jarArchiver.addDirectory(this.classesDirectory, null, null);
        final File jar = new File(this.outputDirectory, this.finalName + ".jar");
        jarArchiver.setDestFile(jar);
        try
        {
            jarArchiver.createArchive();
            muleApplicationArchiveBuilder.addJarLibrary(jar);
        }
        catch (IOException e)
        {
            final String message = "Cannot create project jar";
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }
    }

    private void addDependencies(MuleApplicationArchiveBuilder muleApplicationArchiveBuilder) throws ArchiverException
    {
        for (Artifact artifact : getArtifactsToArchive())
        {
            String message = String.format("Adding <%1s> as a lib", artifact.getId());
            getLog().info(message);
            muleApplicationArchiveBuilder.addJarLibrary(artifact.getFile());
        }
    }

    private Set<Artifact> getArtifactsToArchive()
    {
        ArtifactFilter filter = new ArtifactFilter(this.project, this.inclusions,
            this.exclusions, this.excludeMuleDependencies);
        return filter.getArtifactsToArchive();
    }
}
