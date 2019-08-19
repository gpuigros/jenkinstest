import jenkins.model.*
import hudson.model.*

class DeleteOldJobs  {

    def deleteOld(String folderName) {
        println "in"
        def counter = 0
        def jobs = Jenkins.instance.getAllItems()
        for (job in jobs) {
                println "Job= '${counter++}' '${job.name}"

        }
}
}

