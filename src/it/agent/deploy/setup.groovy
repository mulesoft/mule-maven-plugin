import com.jayway.awaitility.Awaitility

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

muleHome = "target/mule-enterprise-standalone-${muleVersion}"
new File(muleHome + '/conf/mule-agent.jks').delete()
new File(muleHome + '/conf/mule-agent.yml').delete()
muleExecutable = muleHome + "/bin/mule"
amcExecutable = muleHome + "/bin/amc_setup"
process = (amcExecutable + " -I").execute()
process.waitFor()
assert process.exitValue() == 0 : 'Failed to unpack agent'
process = (muleExecutable + " start").execute()
process.waitFor()
assert process.exitValue() == 0 : 'Failed to start Mule'

aCallable = new Callable<Boolean>() {
        public Boolean call() throws Exception {
            println "listening: ${'nc -z localhost 9999'.execute().waitFor()}"
            return "nc -z localhost 9999".execute().waitFor() == 0
        }
    }

Awaitility.await().atMost(2, TimeUnit.MINUTES).until(aCallable)
