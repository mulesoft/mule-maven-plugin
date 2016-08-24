import groovy.json.JsonSlurper

import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

String uri = 'https://anypoint.mulesoft.com'
String ME = "/accounts/api/me";
String LOGIN = "/accounts/login";
String APPLICATION = "/cloudhub/api/applications/maven-plugin-cloudhub-business-group-test";
String environmentsPath = "/accounts/api/organizations/%s/environments";
String AUTHORIZATION_HEADER = "Authorization";
String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";
String BUSINESS_GROUP_ID = '9028da8d-cf4a-4a5f-afeb-38776b836973'
String ENV_ID = '60b01d86-2973-46b1-a788-63ee20cfa7ad'

// Login and get IDs for organization and environment.

def target = ClientBuilder.newClient().target(uri).path(LOGIN);
def json = Entity.json('{"username": "' + username + '", "password": "' + password + '"}');
def response = target.request(MediaType.APPLICATION_JSON_TYPE).
        post(json, String.class);
def bearerToken = new JsonSlurper().parseText(response).access_token

target = ClientBuilder.newClient().target(uri).path(ME);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header("Authorization", "bearer " + bearerToken).get(String.class);
def orgId = new JsonSlurper().parseText(response).user.organization.id;

target = ClientBuilder.newClient().target(uri).path(String.format(environmentsPath, orgId));
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header("Authorization", "bearer " + bearerToken).get(String.class);
def environments = new JsonSlurper().parseText(response).data
def envId = (environments.find { it.name == 'Production'}).id

// Application was deployed.

target = ClientBuilder.newClient().target(uri).path(APPLICATION);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + bearerToken).header(ENV_ID_HEADER, ENV_ID).header(ORG_ID_HEADER, BUSINESS_GROUP_ID).
        get();
assert response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL

// Delete the application.

target = ClientBuilder.newClient().target(uri).path(APPLICATION);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + bearerToken).header(ENV_ID_HEADER, ENV_ID).header(ORG_ID_HEADER, BUSINESS_GROUP_ID).
        delete();
assert response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL

println "response: ${response}"

def repeat = 5 * 60
def deleted = false
while (repeat > 0 && !deleted)
{
    target = ClientBuilder.newClient().target(uri).path(APPLICATION);
    response = target.request(MediaType.APPLICATION_JSON_TYPE).
            header(AUTHORIZATION_HEADER, "bearer " + bearerToken).header(ENV_ID_HEADER, ENV_ID).header(ORG_ID_HEADER, BUSINESS_GROUP_ID).
            get();
    println "response: ${response}"
    deleted = response.getStatus() == Response.Status.NOT_FOUND.statusCode
    println "deleted:  ${deleted}"
    repeat --
}

assert deleted : "Could not delete application"
