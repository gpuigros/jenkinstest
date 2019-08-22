
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
        def job4=dslFactory.job(name){
            deliveryPipelineConfiguration('DEPLOY2')
            scm {
                git {
                    remote {
                        name('remoteB')
                        url('git://github.com/gpuigros/jenkinstest.git')
                    }
                    extensions {
                        wipeOutWorkspace()
                    }
                }
            }
            
            steps {
                maven {
                    goals('-e clean package')
                    mavenInstallation('maven-3.6.0')

                }

            }
        }
    }
}
