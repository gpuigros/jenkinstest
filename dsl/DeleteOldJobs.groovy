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
    def deleteOld(String folderName,String includeRegexp,String excludeRegexp) {
        out.println "Cleaning folder ${folderName}"
        out.println "includeRegexp ${includeRegexp}"
        out.println "excludeRegexp ${excludeRegexp}"
        /**AbstractFolder folder = Jenkins.instance.getAllItems(AbstractFolder.class)
            .find {folder -> folderName == folder.fullName };
        folder.getAllJobs()
            .findAll {job -> job instanceof AbstractProject}
            .findAll {job -> job.isBuildable()}
            .each {out.println "Job= in ${folderName} ${it.name}"};
        **/
        def includePattern = Pattern.compile(includeRegexp)
        def excludePattern = Pattern.compile(excludeRegexp)

        def jobs = Jenkins.instance.getAllItems()
        for (job in jobs) {
                if (!excludePattern.matcher(job.name).matches()){
                    def matcher=includePattern.matcher(job.name)
                    if (matcher!=null && matcher.group()!=null){
                        out.println "Job= ${job} ${counter++} ${job.name} ${job.description}  ${job.displayName}"
                    }
                }

        }
}
}

