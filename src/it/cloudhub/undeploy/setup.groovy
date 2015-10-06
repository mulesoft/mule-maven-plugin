import groovy.json.JsonSlurper
import org.glassfish.jersey.media.multipart.FormDataMultiPart
import org.glassfish.jersey.media.multipart.MultiPart
import org.glassfish.jersey.media.multipart.MultiPartFeature
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart

import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.MediaType

String uri = 'https://anypoint.mulesoft.com'
String ME = "/accounts/api/me";
String LOGIN = "/accounts/login";
String APPLICATIONS = "/cloudhub/api/applications";
String APPLICATION_FILE = "/cloudhub/api/v2/applications/maven-plugin-cloudhub-test/files"
String APPLICATION = "/cloudhub/api/v2/applications/maven-plugin-cloudhub-test"
String STATUS = "/cloudhub/api/applications/maven-plugin-cloudhub-test/status"
String environmentsPath = "/accounts/api/organizations/%s/environments";
String AUTHORIZATION_HEADER = "Authorization";
String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";

// Login and get IDs for organization and environment.

WebTarget target = ClientBuilder.newClient().target(uri).path(LOGIN);
Entity<String> json = Entity.json('{"username": "' + username + '", "password": "' + password + '"}');
def response = target.request(MediaType.APPLICATION_JSON_TYPE).
        post(json, String.class);
def bearerToken = new JsonSlurper().parseText(response).access_token
context.bearerToken = bearerToken

target = ClientBuilder.newClient().target(uri).path(ME);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header("Authorization", "bearer " + bearerToken).get(String.class);
def orgId = new JsonSlurper().parseText(response).user.organization.id;
context.orgId = orgId

target = ClientBuilder.newClient().target(uri).path(String.format(environmentsPath, orgId));
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header("Authorization", "bearer " + bearerToken).get(String.class);
def environments = new JsonSlurper().parseText(response).data
def envId = (environments.find { it.name == 'Production'}).id
context.envId = envId

// Deploy application

String CREATE_REQUEST_TEMPLATE = '''{
    "domain": "maven-plugin-cloudhub-test",
    "region": "us-east-1",
    "muleVersion": "3.7.0",
    "workers": 1,
    "workerType": "Micro",
    "properties": { }
}''';

target = ClientBuilder.newClient().target(uri).path(APPLICATIONS);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + bearerToken).header(ENV_ID_HEADER, envId).header(ORG_ID_HEADER, orgId).
        post(Entity.json(CREATE_REQUEST_TEMPLATE), String.class);
def application = new JsonSlurper().parseText(response)

target = ClientBuilder.newClient().register(MultiPartFeature.class).target(uri).path(APPLICATION_FILE);
FileDataBodyPart applicationPart = new FileDataBodyPart("file", new File("src/it/cloudhub/undeploy/cloudhub-1.1.zip"));
MultiPart multipart = new FormDataMultiPart().bodyPart(applicationPart);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + bearerToken).header(ENV_ID_HEADER, envId).header(ORG_ID_HEADER, orgId).
        post(Entity.entity(multipart, multipart.getMediaType()));
assert response.getStatus() == 200

json = Entity.json('{"status": "START"}')
target = ClientBuilder.newClient().target(uri).path(STATUS);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + bearerToken).header(ENV_ID_HEADER, envId).header(ORG_ID_HEADER, orgId).
        post(json);
assert response.getStatus() == 200

def repeat = 5*60
def deployed = false
while (repeat > 0 && !deployed )
{
    target = ClientBuilder.newClient().target(uri).path(STATUS);
    response = target.request(MediaType.TEXT_PLAIN).
            header(AUTHORIZATION_HEADER, "bearer " + bearerToken).header(ENV_ID_HEADER, envId).header(ORG_ID_HEADER, orgId).
            get();
    assert response.getStatus() == 200
    def entity = response.readEntity(String.class)
    deployed = (entity == 'DEPLOYED' || entity == 'STARTED')
    repeat --
}
assert deployed : 'Application was not deployed after 10 attempts'
