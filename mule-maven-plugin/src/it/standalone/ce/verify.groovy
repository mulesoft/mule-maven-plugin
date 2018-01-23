muleHome = "${basedir}/target/mule-standalone-${muleVersion}"
muleExecutable = muleHome + "/bin/mule"
process = (muleExecutable + " status").execute()
process.waitFor()
statusExitValue = process.exitValue()
statusStdOut = process.in.text

appWasDeployed = new File(muleHome + "/apps/standalone-anchor.txt").exists()

process = (muleExecutable + " stop").execute()
process.waitFor()
stopExitValue = process.exitValue()
stopStdOut = process.in.text

assert 0 == statusExitValue
assert statusStdOut.contains("Mule is running")
assert appWasDeployed
assert 0 == stopExitValue
assert stopStdOut.contains("Stopped Mule.")