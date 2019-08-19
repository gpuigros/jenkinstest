//@Autoapproved


import com.hotelbeds.jenkins.Constants
import org.yaml.snakeyaml.Yaml

import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.pipelines.jobs.releases_mode.ScheduledTriggerJob;
import com.hotelbeds.jenkins.utils.Log

import jenkins.model.*

Yaml yaml = new Yaml()

def pipelineMetadataFileName = "${Constants.DEFAULT_PIPELINES_DIR}/${PIPELINE}"
def pipelineMetadata = yaml.load(readFileFromWorkspace(pipelineMetadataFileName))

Pipeline metaPipeline = new Pipeline(pipelineMetadata)
pipelineYmlFile = "pipeline-" + metaPipeline.getJenkinsPath().split('/').last().toUpperCase() + ".yml"

def pipelinesConfig = yaml.loadAll(readFileFromWorkspace("${Constants.DEFAULT_PROJECT_DIR}/${pipelineYmlFile}"))

File logFile = new File("${WORKSPACE}/logs/scheduled_trigger_regeneration.log")
Log log = Log.configure(logFile)


// Get the out variable
def config = new HashMap()
def bindings = getBinding()
config.putAll(bindings.getVariables())
def out = config['out']


pipelinesConfig.collect().each { pipelineConfig ->
    Pipeline pipeline = new Pipeline(pipelineConfig.pipeline + pipelineMetadata)
    if ("${DEPLOYMENT_DEFAULT_BRANCH}" == 'true') {
        out.println('Regenerating ScheduledTriggerJob with scmCron value: ' + pipeline.scmCron)
        ScheduledTriggerJob scheduledTriggerJob = new ScheduledTriggerJob(pipeline, "${SCM_BRANCH}")
        scheduledTriggerJob.build(this)
    }
}
