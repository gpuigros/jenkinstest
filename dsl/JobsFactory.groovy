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
        String downstreamJob) {
        //def job=dslFactory.job(name)    
        out.println "Job ${job.name} created. Configuring job ${job.name}"

        String config="name: ${name} \n" + "repo: ${gitUrl} \n"  + "command: -e clean package\n"  + "downstreamJob: ${downstreamJob}\n"
        Yaml yaml = new Yaml()
        def pipelineMetadata = yaml.load(config)
        MavenTemplate.create(dslFactory.job(name), pipelineMetadata)

    }
}
