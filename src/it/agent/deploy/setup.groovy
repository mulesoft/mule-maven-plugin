
muleHome = "target/mule-enterprise-standalone-${muleVersion}"
new File(muleHome + '/conf/mule-agent.jks').delete()
new File(muleHome + '/conf/mule-agent.yml').delete()
muleExecutable = muleHome + "/bin/mule"
amcExecutable = muleHome + "/bin/amc_setup"
process = (amcExecutable + " -I").execute()
process.waitFor()
assert process.exitValue() == 0
process = (muleExecutable + " start").execute()
process.waitFor()
assert process.exitValue() == 0


Thread.sleep(30000)