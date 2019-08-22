
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
    //def createJob(FreeStyleJob job) {
    def createJob(
        String name,
        String stageName,
        String gitUrl) {
        
        def job=dslFactory.job(name)    
        out.println "Configuring job ${job.name}"
        job.deliveryPipelineConfiguration("BUILD2")

    }
}
