//@Grab('org.jenkins-ci.plugins:cloudbees-folder:6.9')
//import com.cloudbees.hudson.plugins.folder.AbstractFolder
import jenkins.model.*
import hudson.model.*
import java.util.regex.Pattern



class DeleteOldJobs  {
    def out
    DeleteOldJobs(out){
        this.out=out
    }
    def deleteOld(String folderName,String include,String exclude) {
        out.println "Cleaning folder ${folderName}"
        out.println "includeRegexp ${include}"
        out.println "excludeRegexp ${exclude}"
        /**AbstractFolder folder = Jenkins.instance.getAllItems(AbstractFolder.class)
            .find {folder -> folderName == folder.fullName };
        folder.getAllJobs()
            .findAll {job -> job instanceof AbstractProject}
            .findAll {job -> job.isBuildable()}
            .each {out.println "Job= in ${folderName} ${it.name}"};
        **/
        def includePattern = Pattern.compile(include)
        def excludePattern = Pattern.compile(exclude)

        def jobs = Jenkins.instance.getAllItems()
        for (job in jobs) {
                if (!job.name.contains(exclude) && job.name.contains(include)){
                    out.println "Deleting ${job} ${job.name}"
                    job.delete()
                }

        }
}
}

