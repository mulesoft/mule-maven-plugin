import groovy.json.JsonSlurper

import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType

String uri = 'https://anypoint.mulesoft.com'
String ME = "/accounts/api/me";
String LOGIN = "/accounts/login";
String REGISTRATION = "/hybrid/api/v1/servers/registrationToken"
String environmentsPath = "/accounts/api/organizations/%s/environments";
String AUTHORIZATION_HEADER = "Authorization";
String ENV_ID_HEADER = "X-ANYPNT-ENV-ID";
String ORG_ID_HEADER = "X-ANYPNT-ORG-ID";
String BUSINESS_GROUP_ID = 'ca58c363-1f3b-4e8b-822b-1274aad454c9'
String ENV_ID = '855bf5d8-e13c-44d6-bef8-1beadd20da86'


client = ClientBuilder.newClient();
target = client.target(uri).path(LOGIN);
Entity<String> json = Entity.json('{"username": "' + username + '", "password": "' + password + '"}');
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        post(json, String.class);
def bearerToken = new JsonSlurper().parseText(response).access_token
context.bearerToken = bearerToken

client = ClientBuilder.newClient();
target = client.target(uri).path(REGISTRATION);
response = target.request(MediaType.APPLICATION_JSON_TYPE).
        header(AUTHORIZATION_HEADER, "bearer " + bearerToken).header(ENV_ID_HEADER, ENV_ID).header(ORG_ID_HEADER, BUSINESS_GROUP_ID).
        get(String.class);
def token = new JsonSlurper().parseText(response).data

muleHome = "target/mule-enterprise-standalone-${muleVersion}"
new File(muleHome + '/conf/mule-agent.jks').delete()
new File(muleHome + '/conf/mule-agent.yml').delete()
muleExecutable = muleHome + "/bin/mule"
armExecutable = muleHome + "/bin/amc_setup"
process = (armExecutable + " -H $token server-name").execute()
process.waitFor()
assert process.exitValue() == 0 : 'Couldn\'t register server'
process = (muleExecutable + " start").execute()
process.waitFor()
assert process.exitValue() == 0 : 'Couldn\'t start Mule server'


Thread.sleep(30000)