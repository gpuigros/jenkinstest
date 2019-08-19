package com.hotelbeds.jenkins.pipelines.jobs.templates

import com.hotelbeds.jenkins.pipelines.jobs.JobType

class BuildAndPushDockerImageTemplate implements Templatizable {

    def pipeline
    def command
    def profiles

    BuildAndPushDockerImageTemplate(pipeline, command, profiles) {
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
            name: "BUILD_AND_PUSH_DOCKER_IMAGE_" + stageConfig.name.toUpperCase(),
            showName: "Build and Push Docker Image",
            type: JobType.FREE.code(),
            command: command,
            profiles: profiles
        ]

    }

}
