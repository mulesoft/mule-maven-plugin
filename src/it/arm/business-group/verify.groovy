import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

String uri = 'https://anypoint.mulesoft.com'
String APPLICATIONS = "/hybrid/api/v1/applications";
String AUTHORIZATION_HEADER = "Authorization";
String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";
String SERVERS = "/hybrid/api/v1/servers";
String BUSINESS_GROUP_ID = 'ca58c363-1f3b-4e8b-822b-1274aad454c9'
String ENV_ID = '855bf5d8-e13c-44d6-bef8-1beadd20da86'

def repeat = 60
def application = null
def deployed = false
def response
while (repeat > 0 && (application == null || !deployed) )
{
    client = ClientBuilder.newClient();
    target = client.target(uri).path(APPLICATIONS);
    response = target.request(MediaType.APPLICATION_JSON_TYPE).
            header(AUTHORIZATION_HEADER, "bearer " + context.bearerToken).header(ENV_ID_HEADER, ENV_ID).header(ORG_ID_HEADER, BUSINESS_GROUP_ID).
            get(String.class);
    def applications = new JsonSlurper().parseText(response).data
    application = applications.find { it.artifact.name == 'arm-deploy' }
    deployed = application.desiredStatus == "STARTED"
    repeat --
}
target = client.target(uri).path(APPLICATIONS + "/" + application.id);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + context.bearerToken).header(ENV_ID_HEADER, ENV_ID).header(ORG_ID_HEADER, BUSINESS_GROUP_ID).
        delete()
assert response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL : "Failed to delete application: ${application.id}\n${JsonOutput.prettyPrint(response.readEntity(String.class))}"

target = client.target(uri).path(SERVERS);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + context.bearerToken).header(ENV_ID_HEADER, ENV_ID).header(ORG_ID_HEADER, BUSINESS_GROUP_ID).
        get(String.class)
def serverId = (new JsonSlurper().parseText(response).data.find{ it.name == "server-name"}).id
assert serverId != null : "Server not found"

target = client.target(uri).path(SERVERS + '/' + serverId);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + context.bearerToken).header(ENV_ID_HEADER, ENV_ID).header(ORG_ID_HEADER, BUSINESS_GROUP_ID).
        delete();
assert response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL : "Failed to delete server ${serverId}\n${JsonOutput.prettyPrint(response.readEntity(String.class))}"

def deleted = false
repeat = 60
while (repeat > 0 && !deleted )
{
    target = client.target(uri).path(SERVERS + "/$serverId");
    response = target.request(MediaType.APPLICATION_JSON_TYPE).
            header(AUTHORIZATION_HEADER, "bearer " + context.bearerToken).header(ENV_ID_HEADER, ENV_ID).header(ORG_ID_HEADER, BUSINESS_GROUP_ID).
            get();
    deleted = response.getStatus() == Response.Status.NOT_FOUND.statusCode
    repeat --
}

muleHome = "target/mule-enterprise-standalone-${muleVersion}"
muleExecutable = muleHome + "/bin/mule"
process = (muleExecutable + " stop").execute()
process.waitFor()
assert process.exitValue() == 0 : 'Couldn\'t stop Mule server'
assert deployed : "Application was not deployed"
assert deleted : "Server wasn' deleted"
