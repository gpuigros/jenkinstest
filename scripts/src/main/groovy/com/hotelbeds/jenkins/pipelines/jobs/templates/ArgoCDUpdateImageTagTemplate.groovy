package com.hotelbeds.jenkins.pipelines.jobs.templates

import com.hotelbeds.jenkins.pipelines.jobs.JobType

class ArgoCDUpdateImageTagTemplate implements Templatizable {

    def pipeline
    def command
    def profiles

    ArgoCDUpdateImageTagTemplate(pipeline, command, profiles) {
        this.pipeline = pipeline
        this.command = command
        this.profiles = profiles
    }

    @Override
    def getTemplate(stageConfig) {

        return [
            stage: [
                pipeline: [
                    jenkinsPath: pipeline.jenkinsPath,
                    name       : pipeline.name,
                    recipients : pipeline.recipients
                ]
            ],
            scm: [
                url   : pipeline.scmRepository,
                branch: pipeline.scmBranch
            ],
            name: "UPDATE_IMAGE_TAG_" + stageConfig.name.toUpperCase(),
            showName: "Update Image Tag",
            type: JobType.FREE.code(),
            command: command,
            profiles: profiles
        ]

    }

}
