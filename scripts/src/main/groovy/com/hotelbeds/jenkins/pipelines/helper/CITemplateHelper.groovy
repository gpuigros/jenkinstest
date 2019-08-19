package com.hotelbeds.jenkins.pipelines.helper


import com.hotelbeds.jenkins.pipelines.jobs.templates.*


class CITemplateHelper extends JobTemplateHelper {


    static final String SLAVE_LABEL = "ON_PREMISE"


    CITemplateHelper() {

    }


    @Override
    def configureTemplate(pipeline, stageConfig, jobProfiles, templateParameters) {
        this.templateParameters = templateParameters
        this.profiles = jobProfiles
        this.pipelineConfig = pipeline

        addTemplate(getBuildJob())
        addTemplate(getUnitTestsJob())
        addTemplate(getIntegrationTestsJob())
        addTemplate(getAnalysisJob())

    }


    private Templatizable getBuildJob() {
        def command = templateParameters.buildCommand
        return new CIBuildTemplate(pipelineConfig, command, profiles, SLAVE_LABEL)
    }

    private Templatizable getUnitTestsJob() {
        def command = templateParameters.utCommand
        return new CIUnitTestsTemplate(pipelineConfig, command, profiles, SLAVE_LABEL)
    }

    private Templatizable getIntegrationTestsJob() {
        def command = templateParameters.itCommand
        return new CIIntegrationTestsTemplate(pipelineConfig, command, profiles, SLAVE_LABEL)
    }

    private Templatizable getAnalysisJob() {
        def command = templateParameters.analysisCommand
        return new CIAnalysisTemplate(pipelineConfig, command, profiles, SLAVE_LABEL)
    }


}
