
import jenkins.model.*
import hudson.model.*


class JobsFactory  {
    def out
    def dslFactory
    JobsFactory(out, dslFactory){
        this.out=out
        this.dslFactory=dslFactory
    }
    def createJob(hudson.model.Job job) {

        out.println "Configuring job ${job.name}"
        job.description="My description"
    }
}
