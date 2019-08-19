package com.hotelbeds.jenkins.pipelines.helper

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.jobs.templates.ArgoCDAppSyncTemplate
import com.hotelbeds.jenkins.pipelines.jobs.templates.ArgoCDAppSyncWaitTemplate
import com.hotelbeds.jenkins.pipelines.jobs.templates.ArgoCDTagDockerImageTemplate
import com.hotelbeds.jenkins.pipelines.jobs.templates.ArgoCDUpdateImageTagTemplate
import com.hotelbeds.jenkins.pipelines.jobs.templates.Templatizable

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.bashCommand

class ArgoCDTemplateHelper extends JobTemplateHelper {


    ArgoCDTemplateHelper() {

    }


    @Override
    def configureTemplate(pipeline, stageConfig, jobProfiles, templateParameters) {
        this.templateParameters = templateParameters
        this.profiles = jobProfiles
        this.pipelineConfig = pipeline

        addTemplate(getUpdateImageTagJob())
        addTemplate(getArgoCDAppSyncJob())
        addTemplate(getArgoCDAppSyncWaitJob())
        addTemplate(getTagDockerImageJob())
    }


    private Templatizable getUpdateImageTagJob() {
        def environment = templateParameters.environment
        def command = bashCommand('argocd/updateImageTag.sh', Constants.DEFAULT_PROJECT_DIR, environment)
        return new ArgoCDUpdateImageTagTemplate(pipelineConfig, command, profiles)
    }

    private Templatizable getArgoCDAppSyncJob() {
        def environment = templateParameters.environment
        return new ArgoCDAppSyncTemplate(pipelineConfig, profiles, environment)
    }


    private Templatizable getArgoCDAppSyncWaitJob() {
        def environment = templateParameters.environment
        return new ArgoCDAppSyncWaitTemplate(pipelineConfig, profiles, environment)
    }


    private Templatizable getTagDockerImageJob() {
        def environment = templateParameters.environment
        def command = bashCommand('argocd/tagDockerImage.sh', Constants.DEFAULT_PROJECT_DIR, environment, pipelineConfig.name, Constants.ECR_URL, Constants.ECR_REGION)
        return new ArgoCDTagDockerImageTemplate(pipelineConfig, command, profiles)
    }

}
