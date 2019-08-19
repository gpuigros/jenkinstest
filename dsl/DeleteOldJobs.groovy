import jenkins.model.*
import hudson.model.*

class DeleteOldJobs  {

    def deleteOld(String folderName) {

        println Jenkins.instance.projects.collect { it.name }
}
}

