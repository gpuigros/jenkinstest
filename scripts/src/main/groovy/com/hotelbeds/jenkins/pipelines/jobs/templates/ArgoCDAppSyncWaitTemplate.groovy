package com.hotelbeds.jenkins.pipelines.jobs.templates

import com.hotelbeds.jenkins.pipelines.jobs.JobType
import com.hotelbeds.jenkins.pipelines.jobs.tech_versions.DockerImage

class ArgoCDAppSyncWaitTemplate implements Templatizable {

    def pipeline
    def profiles
    def environment


    ArgoCDAppSyncWaitTemplate(pipeline, profiles, environment) {
        this.pipeline = pipeline
        this.profiles = profiles
        this.environment = environment
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
            name: "ARGOCD_APP_SYNC_WAIT_" + stageConfig.name.toUpperCase(),
            showName: "ArgoCD App Sync Wait...",
            type: JobType.DOCKER.code(),
            image: DockerImage.ARGOCD.name,
            parameters: [
                "app wait " + pipeline.name + "-" + environment + " --insecure"
            ],
            profiles: profiles
        ]

    }

}
