import com.hotelbeds.jenkins.pipelines.jobs.HotfixBuilderJob
import jenkins.model.*

import org.yaml.snakeyaml.Yaml

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.utils.Log

Yaml yaml = new Yaml()

Log.configure(new File("${WORKSPACE}/logs/create_hotfix_builder_job.log"))

def pipelineMetadata = yaml.load(readFileFromWorkspace("pipelines/${PIPELINE}"))
String pipelineName = "${PIPELINE}"
Pipeline pipeline = new Pipeline(pipelineMetadata)
pipeline.name = pipelineName.replaceAll('.yml', '')


if (pipeline.releasementStrategy == Constants.CONTINUOUS_DEPLOYMENT_RELEASEMENT_STRATEGY) {
    def hotfixBuilderJob = new HotfixBuilderJob(pipeline)
    hotfixBuilderJob.build(this)
}
