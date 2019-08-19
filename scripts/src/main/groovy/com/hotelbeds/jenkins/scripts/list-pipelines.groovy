//@Autoapproved

def pipelinesRepoPath = '/tmp/jenkins-pipelines'
def pipelinesRepoFile = new File(pipelinesRepoPath)

if (pipelinesRepoFile.exists()) {
    pipelinesRepoFile.deleteDir()
}

def executeCommand = { command ->
    def sout = new StringBuilder(), serr = new StringBuilder()
    def proc = command.execute()
    proc.consumeProcessOutput(sout, serr)
    proc.waitForOrKill(1000)
    return sout
}

def sout1 = executeCommand("git clone -b master --single-branch ssh://git@stash.hotelbeds:7999/envtools/jenkins-pipelines.git ${pipelinesRepoPath}")
def sout2 = executeCommand("find ${pipelinesRepoPath}  -printf %f\\n")

def pipelines = sout2.tokenize("\n")
pipelines.findAll { it.toString().endsWith('.yml') }.sort()
