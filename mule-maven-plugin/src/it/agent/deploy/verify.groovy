
muleHome = "target/mule-enterprise-standalone-${muleVersion}"

def repeat = 10;
def deployed = false;

while (repeat > 0 && !deployed)
{
    deployed = new File(muleHome + '/apps/agent-anchor.txt').exists();
    Thread.sleep(1000);
    repeat--;
}

muleExecutable = muleHome + "/bin/mule"
process = (muleExecutable + " stop").execute()
process.waitFor()

assert deployed;
