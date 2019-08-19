package com.hotelbeds.jenkins.pipelines.jobs.templates

import com.hotelbeds.jenkins.pipelines.jobs.JobType

class ArgoCDTagDockerImageTemplate implements Templatizable {

    def pipeline
    def command
    def profiles

    ArgoCDTagDockerImageTemplate(pipeline, command, profiles) {
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
            name: "TAG_DOCKER_IMAGE_" + stageConfig.name.toUpperCase(),
            showName: "Tag image in Registry ",
            type: JobType.FREE.code(),
            command: command,
            profiles: profiles
        ]

    }

}
