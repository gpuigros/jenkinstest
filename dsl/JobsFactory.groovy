
import jenkins.model.*
import hudson.model.*


class JobsFactory  {
    def out
    JobsFactory(out){
        this.out=out
    }
    def createJob(
        String name,
        String stageName,
        String gitUrl) {

        out.println "Creating job ${name}"
        def job=job(name) {
                            deliveryPipelineConfiguration(stageName)
                            scm {
                                git {
                                    remote {
                                        name('remote')
                                        url(gitUrl)
                                    }
                                    extensions {
                                        wipeOutWorkspace()
                                    }
                                }
                            }       
                        }
    }
}
