muleHome = "${basedir}/target/mule-enterprise-standalone-${muleVersion}"
muleExecutable = muleHome + "/bin/mule"
process = (muleExecutable + " status").execute()
process.waitFor()

statusExitValue = process.exitValue()
statusStdOut = process.in.text

assert 1 == statusExitValue
assert statusStdOut.contains("Mule Enterprise Edition is not running.")
