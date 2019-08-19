package com.hotelbeds.jenkins.pipelines.regenerator

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Buildable
import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.pipelines.Validable

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.getScript


abstract class RegeneratorJob implements Buildable, Validable {

    Pipeline pipeline

    String pipelineFileName

    def jobName

    def jenkinsJobName

    String ymlFile

    def nextJob

    RegeneratorJob(pipeline, pipelineFileName) {
        this.pipeline = pipeline
        this.pipelineFileName = pipelineFileName

        if (this.pipeline.releasementStrategy == Constants.RELEASES_RELEASEMENT_STRATEGY) {
            ymlFile = "pipeline-" + this.pipeline?.jenkinsPath?.split('/')?.last()?.toUpperCase() + ".yml"
        } else {
            ymlFile = "pipeline.yml"
        }
    }


    /**
     * Inits the DSL job.
     *
     * @param dslFactory the dsl factory
     * @return the dsl job
     */
    def init(dslFactory) {
        jobName = "${this.pipeline?.name}_REGENERATOR"
        jenkinsJobName = "${this.pipeline?.jenkinsPath}/$jobName"
        nextJob = jenkinsJobName.replaceAll('REGENERATOR', 'BUILD')

        if (!isValid()) {
            throw new Error("$jenkinsJobName has validation errors")
        }

        dslFactory.folder(this.pipeline?.jenkinsPath) {
            displayName(this.pipeline?.jenkinsPath?.split('/')?.last()?.toUpperCase())
        }

        return dslFactory.job(jenkinsJobName)
    }


    protected String prepareEnvironmentToRegeneratePipeline(dslFactory) {
        String buildPipelineEnv = dslFactory.readFileFromWorkspace(Constants.PIPELINE_BUILD_SCRIPTS_PATH + 'BuildPipelineEnv.groovy')
        buildPipelineEnv = buildPipelineEnv.replaceAll("##DEFAULT_PROJECT_DIR##", Constants.DEFAULT_PROJECT_DIR)
        buildPipelineEnv = buildPipelineEnv.replaceAll("##DEFAULT_SCRIPTS_DIR##", Constants.DEFAULT_SCRIPTS_DIR)
        buildPipelineEnv = buildPipelineEnv.replaceAll("##YML_FILE##", this.ymlFile)
        return buildPipelineEnv
    }

    protected String getRegeneratorEnvironmentPropertiesScript(dslFactory) {
        def regeneratorEnvInjectScriptParams = [
                "##PIPELINE_JENKINS_PATH##": this.pipeline.jenkinsPath,
                "##DEFAULT_PROJECT_DIR##"  : Constants.DEFAULT_PROJECT_DIR,
                "##YML_FILE##"             : this.ymlFile
        ]
        return getScript(dslFactory, 'envinject-regenerator.groovy', regeneratorEnvInjectScriptParams)
    }

    @Override
    boolean isValid() {
        return true
    }

}
