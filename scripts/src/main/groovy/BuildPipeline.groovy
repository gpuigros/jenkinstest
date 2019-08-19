//@Autoapproved


import com.hotelbeds.jenkins.Constants
import org.yaml.snakeyaml.Yaml

import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.utils.Log

import jenkins.model.*

Yaml yaml = new Yaml()

def pipelinesConfig = yaml.loadAll(readFileFromWorkspace("${Constants.DEFAULT_PROJECT_DIR}/pipeline.yml"))

File logFile = new File("${WORKSPACE}/logs/pipeline_generation.log")
Log log = Log.configure(logFile)

pipelinesConfig.collect().each { pipelineConfig ->
    def pipelineMetadata = yaml.load(readFileFromWorkspace("${Constants.DEFAULT_PIPELINES_DIR}/${pipelineConfig.pipeline.name}.yml"))
    Pipeline pipeline = new Pipeline(pipelineConfig.pipeline + pipelineMetadata)
    pipeline.configureSharedArtifacts()
    pipeline.configureRdmNotifications()
    pipeline.build(this)
}
