import jenkins.model.*

import org.yaml.snakeyaml.Yaml

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.pipelines.jobs.releases_mode.ScheduledTriggerJob
import com.hotelbeds.jenkins.utils.Log

Yaml yaml = new Yaml()

Log.configure(new File("${WORKSPACE}/logs/create_scheduled_trigger_job.log"))

def pipelineMetadata = yaml.load(readFileFromWorkspace("pipelines/${PIPELINE}"))
String pipelineName = "${PIPELINE}"
String scmBranch = "${SCM_BRANCH}"

Pipeline pipeline = new Pipeline(pipelineMetadata)
pipeline.name = (pipelineName.contains(Constants.BRANCH_SEPARATOR) ? pipelineName.split(Constants.BRANCH_SEPARATOR).first() : pipelineName).replaceAll('.yml', '')

if (!pipelineName.contains(Constants.BRANCH_PLACEHOLDER)) {
    scmBranch = pipeline.scmBranch
} else {
    if (pipelineName.contains("HOTFIX_BRANCH")) {
        scmBranch = "master"
    }
}

if (pipeline.releasementStrategy == Constants.RELEASES_RELEASEMENT_STRATEGY) {
    def scheduledTriggerJob = new ScheduledTriggerJob(pipeline, scmBranch)
    scheduledTriggerJob.build(this)
}
