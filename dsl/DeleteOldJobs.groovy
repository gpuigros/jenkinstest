import jenkins.model.*
import hudson.model.*
def out = getBinding().out;

class DeleteOldJobs  {

    def deleteOld(String folderName) {
        println "in"
        def counter = 0
        def jobs = Jenkins.instance.getAllItems()
        for (job in jobs) {
                out.println "Job= '${counter++}' '${job.name}"

        }
}
}

