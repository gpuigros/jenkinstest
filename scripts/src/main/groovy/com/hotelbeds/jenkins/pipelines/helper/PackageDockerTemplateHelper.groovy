package com.hotelbeds.jenkins.pipelines.helper

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.jobs.templates.*

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.bashCommand

class PackageDockerTemplateHelper extends JobTemplateHelper {


    PackageDockerTemplateHelper() {

    }


    @Override
    def configureTemplate(pipeline, stageConfig, jobProfiles, templateParameters) {
        this.templateParameters = templateParameters
        this.profiles = jobProfiles
        this.pipelineConfig = pipeline

        addTemplate(getBuildAndPushDockerImageJob())
    }


    private Templatizable getBuildAndPushDockerImageJob() {
        def command = bashCommand('docker/buildAndPush.sh', Constants.DEFAULT_PROJECT_DIR, pipelineConfig.name, Constants.ECR_URL, Constants.ECR_REGION)
        return new BuildAndPushDockerImageTemplate(pipelineConfig, command, profiles)
    }


}
