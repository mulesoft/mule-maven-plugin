muleHome = "${basedir}/target/mule-enterprise-standalone-${muleVersion}"
muleExecutable = muleHome + "/bin/mule"

process = (muleExecutable + " status").execute()
process.waitFor()
assert 0 == process.exitValue()
assert process.in.text.contains("Mule Enterprise Edition is running")

assert new File(muleHome + "/apps/app2-anchor.txt").exists()

process = (muleExecutable + " stop").execute()
process.waitFor()
assert 0 == process.exitValue()
assert process.in.text.contains("Stopped Mule Enterprise Edition.")

new File(muleHome + "/apps").deleteDir()
new File(muleHome + "/apps").mkdir()