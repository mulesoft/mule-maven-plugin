Mule Maven Plugin
=================

TODO: re write this!!!!!

The Mule ESB Maven Tools allow the development of Mule applications based on Maven tooling. This kit includes archetypes for building regular Mule applications, Mule domains and Mule domain bundles.

Maven Configuration
----------------------------------------

For it to work you need to add some entries to your settings.xml file.
First, add a new profile with the following repositories and pluginRepositories:

     <profiles>
         ...
         <profile>
            <id>mule-extra-repos</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <repositories>
                <repository>
                    <id>mule-public</id>
                    <url> https://repository.mulesoft.org/nexus/content/repositories/public </url>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <id>mule-public</id>
                    <url> https://repository.mulesoft.org/nexus/content/repositories/public </url>
                </pluginRepository>
            </pluginRepositories>
         </profile>
         ...
     </profiles>

Finally, as a pluginGroup you should add:

     <pluginGroups>
        ...
        <pluginGroup>org.mule.tools</pluginGroup>
        ...
     </pluginGroups>

Once you have your app or domain you can use -Dmule.home to specify the path to your Mule instance where they should be installed. Alternatively, $MULE_HOME will be used.





