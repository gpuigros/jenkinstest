@Grab('org.yaml:snakeyaml:1.17')
import jenkins.model.*
import hudson.model.*
import javaposse.jobdsl.dsl.jobs.FreeStyleJob
import org.yaml.snakeyaml.Yaml
import templates.*

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
        String gitUrl,
        String upstreamJob) {
        //def job=dslFactory.job(name)    
         
        String config="name: ${name} \n" + "stageName: ${stageName} \n" +  "repo: ${gitUrl} \n"  + "command: -e clean package\n"  + "upstreamJob: ${upstreamJob}\n"
        Yaml yaml = new Yaml()
        def pipelineMetadata = yaml.load(config)
        FreeStyleJob job=dslFactory.job(name)
        MavenTemplate.create(job, pipelineMetadata)
        out.println "Job ${job.name} created."
    }
}
