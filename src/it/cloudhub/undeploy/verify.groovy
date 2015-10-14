import groovy.json.JsonSlurper

import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

String uri = 'https://anypoint.mulesoft.com'
String APPLICATION = "/cloudhub/api/applications/maven-plugin-cloudhub-undeploy";
String APPLICATIONS = "/cloudhub/api/applications";
String AUTHORIZATION_HEADER = "Authorization";
String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";

def repeat = 5 * 60
def undeployed = false
while (repeat > 0 && !undeployed)
{
    def target = ClientBuilder.newClient().target(uri).path(APPLICATIONS);
    response = target.request(MediaType.APPLICATION_JSON_TYPE).
            header(AUTHORIZATION_HEADER, "bearer " + context.bearerToken).header(ENV_ID_HEADER, context.envId).header(ORG_ID_HEADER, context.orgId).
            get(String.class);
    def applications = new JsonSlurper().parseText(response)
    application = applications.find { it.domain == 'maven-plugin-cloudhub-undeploy' }
    undeployed = (application == null || application.status == 'UNDEPLOYED')
    repeat--
}
assert undeployed : "Application was not undeployed."

// Delete application
target = ClientBuilder.newClient().target(uri).path(APPLICATION);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + context.bearerToken).header(ENV_ID_HEADER, context.envId).header(ORG_ID_HEADER, context.orgId).
        delete();
assert response.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL : "Could not delete application"
