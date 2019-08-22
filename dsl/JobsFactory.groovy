
import jenkins.model.*
import hudson.model.*
import javaposse.jobdsl.dsl.jobs.FreeStyleJob

class JobsFactory  {
    def out
    def dslFactory
    JobsFactory(out, dslFactory){
        this.out=out
        this.dslFactory=dslFactory
    }
    def createJob(FreeStyleJob job) {

        out.println "Configuring job ${job.name}"
        job.description="My description"
    }
}
