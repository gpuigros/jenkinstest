import jenkins.model.*
import hudson.model.*

class DeleteOldJobs  {

    def deleteOld(String folderName) {
        println "in"
        counter = 0
        jobs = Jenkins.instance.getAllItems()
        for (job in jobs) {
                println "Job= '${counter++}' '${job.name}' scm '${job.scm}'"

        }
}
}

