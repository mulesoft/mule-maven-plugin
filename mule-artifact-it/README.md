Mule Maven Plugin Integration Tests
===================================

This is the integration test project to validate the Mule Maven Plugin

It is composed by several projects: 
* mule-packager-it
* mule-deployer-it

## mule-packager-it

## mule-deployer-it

**Warning**: Please notice, this project depends heavily in Maven and system properties to run. As a result some test may not run 
properly from your IDE.

```console
  mvn clean install
 ```
                                                             
 In order to run mule-deploy-it you need to define the following env vars:
 * username: username of your Anypoint account 
 * passowrd: passowrd of your Anypoint account
 * mule.version: version of the Mule Runtime you which to run your tests against. This is optional if not define it will assume *4.0.0*
 
 ```console
   mvn clean test -Dusername=fakseuser -Dpassword=fakepassword -Dmule.version=4.0.0
  ```
