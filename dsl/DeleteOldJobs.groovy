import jenkins.model.*
import hudson.model.*



class DeleteOldJobs  {
    def out = getBinding().out;
    def deleteOld(String folderName) {
        println "in"
        def counter = 0
        def jobs = Jenkins.instance.getAllItems()
        for (job in jobs) {
                out.println "Job= '${counter++}' '${job.name}"

        }
}
}

