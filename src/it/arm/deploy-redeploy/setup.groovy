import com.jayway.awaitility.Awaitility
import groovy.json.JsonSlurper

import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

String uri = 'https://anypoint.mulesoft.com'
String ME = "/accounts/api/me";
String LOGIN = "/accounts/login";
String REGISTRATION = "/hybrid/api/v1/servers/registrationToken"
String environmentsPath = "/accounts/api/organizations/%s/environments";
String SERVERS = "/hybrid/api/v1/servers";
String AUTHORIZATION_HEADER = "Authorization";
String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";

client = ClientBuilder.newClient();
target = client.target(uri).path(LOGIN);
Entity<String> json = Entity.json('{"username": "' + username + '", "password": "' + password + '"}');
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        post(json, String.class);
def bearerToken = new JsonSlurper().parseText(response).access_token
context.bearerToken = bearerToken

client = ClientBuilder.newClient();
target = client.target(uri).path(ME);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header("Authorization", "bearer " + bearerToken).get(String.class);
def orgId = new JsonSlurper().parseText(response).user.organization.id;
context.orgId = orgId

client = ClientBuilder.newClient();
target = client.target(uri).path(String.format(environmentsPath, orgId));
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header("Authorization", "bearer " + bearerToken).get(String.class);
def environments = new JsonSlurper().parseText(response).data
def envId = (environments.find { it.name == 'Production' }).id
context.envId = envId

client = ClientBuilder.newClient();
target = client.target(uri).path(REGISTRATION);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + bearerToken).header(ENV_ID_HEADER, envId).header(ORG_ID_HEADER, orgId).
        get(String.class);
def token = new JsonSlurper().parseText(response).data

muleHome = "target/mule-enterprise-standalone-${muleVersion}"
new File(muleHome + '/conf/mule-agent.jks').delete()
new File(muleHome + '/conf/mule-agent.yml').delete()
muleExecutable = muleHome + "/bin/mule"
def start = muleExecutable + " start"
process = (muleHome + "/bin/amc_setup -H $token server-name-deploy-redeploy").execute()
assert process.waitFor() == 0 : 'Couldn\'t register server'
assert start.execute().waitFor() == 0 : 'Couldn\'t start Mule server'

target = ClientBuilder.newClient().target(uri).path(SERVERS);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + context.bearerToken).header(ENV_ID_HEADER, envId).header(ORG_ID_HEADER, orgId).
        get(String.class)
def serverId = (new JsonSlurper().parseText(response).data.find{ it.name == "server-name-deploy-redeploy"}).id
assert serverId != null : "Server not found"

serverIsRunning = new Callable<Boolean>() {
    public Boolean call() throws Exception {
        def target = ClientBuilder.newClient().target(uri).path(SERVERS + "/$serverId");
        def response = target.request(MediaType.APPLICATION_JSON_TYPE).
                header(AUTHORIZATION_HEADER, "bearer " + bearerToken).header(ENV_ID_HEADER, envId).header(ORG_ID_HEADER, orgId).
                get(String.class);
        return new JsonSlurper().parseText(response).data.status == 'RUNNING'
    }
}

Awaitility.await().atMost(4, TimeUnit.MINUTES).until(serverIsRunning)
