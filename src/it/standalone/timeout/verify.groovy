muleHome = "${basedir}/target/mule-enterprise-standalone-${muleVersion}"
muleExecutable = muleHome + "/bin/mule"

process = (muleExecutable + " status").execute()
process.waitFor()
assert 1 == process.exitValue()
assert process.in.text.contains("Mule Enterprise Edition is not running")
assert !new File(muleHome + "/apps/timeout-anchor.txt").exists()

new File(muleHome + "/apps").deleteDir()
new File(muleHome + "/apps").mkdir()
