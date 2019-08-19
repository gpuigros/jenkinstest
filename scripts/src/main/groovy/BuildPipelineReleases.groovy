//@Autoapproved


import com.hotelbeds.jenkins.Constants
import org.yaml.snakeyaml.Yaml

import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.utils.Log

import jenkins.model.*

Yaml yaml = new Yaml()

def pipelineMetadataFileName = "${Constants.DEFAULT_PIPELINES_DIR}/${PIPELINE}"
def pipelineMetadata = yaml.load(readFileFromWorkspace(pipelineMetadataFileName))

Pipeline metaPipeline = new Pipeline(pipelineMetadata)

pipelineYmlFile = "pipeline-" + metaPipeline.getJenkinsPath().split('/').last().toUpperCase() + ".yml"

def pipelinesConfig = yaml.loadAll(readFileFromWorkspace("${Constants.DEFAULT_PROJECT_DIR}/${pipelineYmlFile}"))

File logFile = new File("${WORKSPACE}/logs/pipeline_generation.log")
Log log = Log.configure(logFile)

pipelinesConfig.collect().each { pipelineConfig ->

    Pipeline pipeline = new Pipeline(pipelineConfig.pipeline + pipelineMetadata)

    // Replace branch placeholder
    if (pipeline.scmBranch.contains(Constants.BRANCH_PLACEHOLDER)) {
        pipeline.scmBranch = "${SCM_BRANCH}"
    }

    pipeline.configureSharedArtifacts()
    pipeline.configureRdmNotifications()
    pipeline.build(this)
}
