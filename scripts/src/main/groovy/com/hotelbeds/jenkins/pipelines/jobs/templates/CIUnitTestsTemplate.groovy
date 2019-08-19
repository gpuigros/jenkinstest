package com.hotelbeds.jenkins.pipelines.jobs.templates

import com.hotelbeds.jenkins.pipelines.jobs.JobType

class CIUnitTestsTemplate implements Templatizable {

    def pipeline
    def command
    def profiles
    def jenkinsSlaveLabel

    CIUnitTestsTemplate(pipeline, command, profiles, jenkinsSlaveLabel) {
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
            name: "UNIT_TESTS",
            showName: "Unit tests",
            type: JobType.MAVEN.code(),
            jenkinsAgentTags: jenkinsSlaveLabel,
            command: command,
            profiles: profiles,
            sharedArtifacts: [
                [
                    "CODE_ANALYSIS": [
                        '**/target/coverage-reports/jacoco-ut.exec',
                        '**/target/surefire-reports/*.xml',
                        '**/target/classes/**/*.class'
                    ]
                ]
            ],
            publishers: [
                junit: [
                    '**/target/surefire-reports/*.xml'
                ]
            ]
        ]

    }

}
