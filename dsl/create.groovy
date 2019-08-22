 @Grab('org.yaml:snakeyaml:1.17')
import DeleteOldJobs
import JobsFactory
import templates.*
import org.yaml.snakeyaml.Yaml

def out= getBinding().out;
def deleteJobs=new DeleteOldJobs(out)
def jobsFactory=new JobsFactory(out,this)
def basePath="${PARENT_FOLDER}/"


deleteJobs.deleteOld("${PARENT_FOLDER}","${PARENT_FOLDER}_", "REGENERATOR")


println "basePath = ${basePath}"
//def job11=job("${basePath}_job-dsl-test_BUILD_0")
//jobsFactory.createJob(job11)

def pipelineFile="${WORKSPACE}/pipeline.yml"

println "Processing file " + pipelineFile
Yaml yaml = new Yaml()
def pipelineMetadata = yaml.load(new FileReader(pipelineFile) )
println pipelineMetadata

def projectName=pipelineMetadata.pipeline.name
println "Processing pipeline ${projectName}"


def triggerJob=''
pipelineMetadata.pipeline.stages.each { stage ->
    println "Procesing Stage ${stage.name}"
    stage.jobs.each { job ->
        println "Procesing Job ${job.name}"
        def fullJobName="${basePath}${projectName}_${job.name}"
        jobsFactory.createJob(
            "${fullJobName}",
            stage.name,
            pipelineMetadata.pipeline.repo,
            "'${triggerJob}'")
        triggerJob=fullJobName
    };
};

