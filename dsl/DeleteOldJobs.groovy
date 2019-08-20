import jenkins.model.*
import hudson.model.*



class DeleteOldJobs  {
    def out
    DeleteOldJobs(out){
        this.out=out
    }
    def deleteOld(String folderName) {
        out.println "Cleaning folder ${folderName}"
        AbstractFolder folder = Jenkins.instance.getAllItems(AbstractFolder.class)
            .find {folder -> folderName == folder.fullName };
        folder.getAllJobs()
            .findAll {job -> job instanceof AbstractProject}
            .findAll {job -> job.isBuildable()}
            .each {out.println "Job= in ${folderName} ${it.name}"};

        println "in"
        def counter = 0
        def jobs = Jenkins.instance.getAllItems()
        for (job in jobs) {
                //out.println "Job= '${counter++}' '${job.name}"
                out.println "Job= ${job} ${counter++} ${job.name} ${job.description}  ${job.displayName}"

        }
}
}

