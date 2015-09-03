muleHome = "${basedir}/target/mule-enterprise-standalone-${muleVersion}"
muleExecutable = muleHome + "/bin/mule"

process = (muleExecutable + " status").execute()
process.waitFor()
assert 0 == process.exitValue() : "Status failed"
assert process.in.text.contains("Mule Enterprise Edition is running")

process = (muleExecutable + " stop").execute()
process.waitFor()
assert 0 == process.exitValue() : "Stop failed"
assert process.in.text.contains("Stopped Mule Enterprise Edition.")

assert new File(muleHome + "/domains/domain/mule-domain-config.xml").exists()
assert new File(muleHome + "/domains/domain-anchor.txt").exists()