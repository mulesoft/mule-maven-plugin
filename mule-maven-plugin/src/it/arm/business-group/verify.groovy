import com.jayway.awaitility.Awaitility
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

String uri = 'https://anypoint.mulesoft.com'
String APPLICATIONS = "/hybrid/api/v1/applications";
String AUTHORIZATION_HEADER = "Authorization";
String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";
String SERVERS = "/hybrid/api/v1/servers";
String BUSINESS_GROUP_ID = '9028da8d-cf4a-4a5f-afeb-38776b836973'
String ENV_ID = 'b9396529-7a09-4cb4-a962-cd8c9aec3266'

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
    application = applications.find { it.artifact.name == 'arm-deploy-business-group' }
    deployed = application.desiredStatus == "STARTED"
    repeat --
}

target = client.target(uri).path(APPLICATIONS + "/" + application.id);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + context.bearerToken).header(ENV_ID_HEADER, ENV_ID).header(ORG_ID_HEADER, BUSINESS_GROUP_ID).
        delete()
assert response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL : "Failed to delete application: ${application.id}\n${JsonOutput.prettyPrint(response.readEntity(String.class))}"

// Begin Workaround, delete once CHHYBRID-987 is fixed
def deleted = false
repeat = 60
while (repeat > 0 && !deleted)
{
    target = client.target(uri).path(APPLICATIONS + "/" + application.id);
    response = target.request(MediaType.APPLICATION_JSON_TYPE).
            header(AUTHORIZATION_HEADER, "bearer " + context.bearerToken).header(ENV_ID_HEADER, ENV_ID).header(ORG_ID_HEADER, BUSINESS_GROUP_ID).
            get();
    deleted = response.getStatus() == Response.Status.NOT_FOUND.statusCode
    repeat --
}
// Workaround end

target = client.target(uri).path(SERVERS);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + context.bearerToken).header(ENV_ID_HEADER, ENV_ID).header(ORG_ID_HEADER, BUSINESS_GROUP_ID).
        get(String.class)
def serverId = (new JsonSlurper().parseText(response).data.find{ it.name == "server-name-business-group"}).id
assert serverId != null : "Server not found"

target = client.target(uri).path(SERVERS + '/' + serverId);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + context.bearerToken).header(ENV_ID_HEADER, ENV_ID).header(ORG_ID_HEADER, BUSINESS_GROUP_ID).
        delete();
assert response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL : "Failed to delete server ${serverId}\nStatus code: ${response.getStatus()}\n${JsonOutput.prettyPrint(response.readEntity(String.class))}"

deleted = false
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

def muleExecutable = "target/mule-enterprise-standalone-${muleVersion}/bin/mule"
def status = muleExecutable + " status"
def stop = muleExecutable + " stop"
assert stop.execute().waitFor() == 0 : 'Couldn\'t stop Mule server'
assert deployed : "Application was not deployed"
assert deleted : "Server wasn't deleted"

aCallable = new Callable<Boolean>() {
    public Boolean call() throws Exception {
        return status.execute().waitFor() == 1
    }
}

Awaitility.await().atMost(2, TimeUnit.MINUTES).until(aCallable)
