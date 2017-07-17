muleHome = "${basedir}/target/mule-enterprise-standalone-${muleVersion}"
muleExecutable = muleHome + "/bin/mule"

process = (muleExecutable + " status").execute()
process.waitFor()
assert 1 == process.exitValue() : "Mule was running"
assert process.in.text.contains("Mule Enterprise Edition is not running")

assert !new File(muleHome + "/apps/app1-anchor.txt").exists() : "app1 was running"
assert !new File(muleHome + "/apps/app2-anchor.txt").exists()  : "app2 was running"

(muleExecutable + " stop").execute().waitFor() // just in case

new File(muleHome + "/apps").deleteDir()
new File(muleHome + "/apps").mkdir()