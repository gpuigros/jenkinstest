package com.hotelbeds.jenkins.pipelines.jobs.templates

import com.hotelbeds.jenkins.pipelines.jobs.JobType

class CIIntegrationTestsTemplate implements Templatizable {

    def pipeline
    def command
    def profiles
    def jenkinsSlaveLabel

    CIIntegrationTestsTemplate(pipeline, command, profiles, jenkinsSlaveLabel) {
        this.pipeline = pipeline
        this.command = command
        this.profiles = profiles
        this.jenkinsSlaveLabel = jenkinsSlaveLabel
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
            name: "INTEGRATION_TESTS",
            showName: "Integration tests",
            type: JobType.MAVEN.code(),
            jenkinsAgentTags: jenkinsSlaveLabel,
            command: command,
            profiles: profiles,
            publishers: [
                junit: [
                    '**/target/failsafe-reports/*.xml'
                ]
            ]
        ]

    }

}
