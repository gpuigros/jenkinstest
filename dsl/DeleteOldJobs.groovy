import jenkins.model.*
import hudson.model.*



class DeleteOldJobs  {
    def out
    DeleteOldJobs(out){
        this.out=out
    }
    def deleteOld(String folderName) {
        println "in"
        def counter = 0
        def jobs = Jenkins.instance.getAllItems()
        for (job in jobs) {
                //out.println "Job= '${counter++}' '${job.name}"
                out.println "Job= ${job} ${counter++} ${job.name} ${description}  ${displayName} ${properties}"

        }
}
}

