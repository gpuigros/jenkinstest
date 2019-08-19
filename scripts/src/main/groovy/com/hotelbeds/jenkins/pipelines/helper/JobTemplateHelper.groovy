package com.hotelbeds.jenkins.pipelines.helper

import com.hotelbeds.jenkins.pipelines.jobs.templates.Templatizable;


abstract class JobTemplateHelper implements Configurable {

    def templates = []

    def pipelineConfig
    def templateParameters
    def profiles

    JobTemplateHelper() {

    }

    def validate(templateParameters, jobTemplate) {
        if (jobTemplate.requiredParameters.size() > 0) {
            jobTemplate.requiredParameters.each { requiredParameter ->
                checkRequiredParameter(templateParameters, requiredParameter, jobTemplate.name())
            }
        }
    }

    def checkRequiredParameter(templateParameters, requiredParameter, templateName) {
        if (!templateParameters.containsKey(requiredParameter)) {
            throw new Error("${requiredParameter} parameter is required to use ${templateName} job template")
        }
    }

    def addJobsFromTemplates(pipeline, stageConfig, jobProfiles, templateIndex, templateParameters) {
        configureTemplate(pipeline, stageConfig, jobProfiles, templateParameters)
        templates.each { template ->
            addJob(templateIndex++, template, stageConfig)
        }
    }

    def addJob(templateIndex, Templatizable template, stageConfig) {
        stageConfig.jobs.addAll(templateIndex, template.getTemplate(stageConfig))
    }

    def addTemplate(Templatizable jobTemplate) {
        templates.add(jobTemplate)
    }

}
