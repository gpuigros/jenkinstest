
import jenkins.model.*
import hudson.model.*


class JobsFactory  {
    def out
    def dslFactory
    JobsFactory(out, dslFactory){
        this.out=out
        this.dslFactory=dslFactory
    }
    def createJob(
        String name,
        String stageName,
        String gitUrl) {

        out.println "Creating job ${name}"
        def job=dslFactory.job(name) {
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
