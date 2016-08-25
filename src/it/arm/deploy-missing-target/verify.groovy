import groovy.json.JsonSlurper

import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType

String uri = 'https://anypoint.mulesoft.com'
String APPLICATIONS = "/hybrid/api/v1/applications";
String AUTHORIZATION_HEADER = "Authorization";
String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";
String ME = "/accounts/api/me";
String LOGIN = "/accounts/login";
String environmentsPath = "/accounts/api/organizations/%s/environments";

client = ClientBuilder.newClient();
target = client.target(uri).path(LOGIN);
Entity<String> json = Entity.json('{"username": "' + username + '", "password": "' + password + '"}');
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        post(json, String.class);
def bearerToken = new JsonSlurper().parseText(response).access_token

client = ClientBuilder.newClient();
target = client.target(uri).path(ME);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header("Authorization", "bearer " + bearerToken).get(String.class);
def orgId = new JsonSlurper().parseText(response).user.organization.id;

client = ClientBuilder.newClient();
target = client.target(uri).path(String.format(environmentsPath, orgId));
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header("Authorization", "bearer " + bearerToken).get(String.class);
def environments = new JsonSlurper().parseText(response).data
def envId = (environments.find { it.name == 'Production' }).id

client = ClientBuilder.newClient();
target = client.target(uri).path(APPLICATIONS);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + bearerToken).header(ENV_ID_HEADER, envId).header(ORG_ID_HEADER, orgId).get(String.class);
def applications = new JsonSlurper().parseText(response).data

def application = applications.find { it.artifact.name == 'arm' }
assert application == null : 'Application was deployed.'
