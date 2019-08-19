package com.hotelbeds.jenkins.pipelines.jobs

import com.hotelbeds.jenkins.pipelines.helper.JobTemplateHelper


class JobTemplate {

    def templateIndex
    def templateName
    def templateParameters
    JobTemplateHelper templateHelper
    JobTemplateType jobTemplateType



    JobTemplate(templateIndex, templateName, templateParameters) {
        this.templateIndex = templateIndex
        this.templateName = templateName
        this.templateParameters = templateParameters

        try {
            this.jobTemplateType = JobTemplateType.valueOf(templateName)
        } catch (IllegalArgumentException e) {
            throw new Error("${templateName} is not a valid job template")
        }

        this.templateHelper = jobTemplateType.templateHelper
    }

    def configure(pipeline, stageConfig) {

        //Validate template required parameters
        templateHelper.validate(templateParameters, JobTemplateType.valueOf(templateName))

        //Get template profiles to set them in new jobs before remove the job
        def jobProfiles = stageConfig.jobs[templateIndex].profiles

        //Remove the jobTemplate
        stageConfig.jobs.remove(templateIndex)

        //Add real jobs
        templateHelper.addJobsFromTemplates(pipeline, stageConfig, jobProfiles, templateIndex, templateParameters)

    }

}
