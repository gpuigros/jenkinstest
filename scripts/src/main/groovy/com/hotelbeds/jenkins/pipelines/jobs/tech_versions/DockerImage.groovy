package com.hotelbeds.jenkins.pipelines.jobs.tech_versions

enum DockerImage {

    SERVERLESS('SERVERLESS', 'serverless', 'serverless-slave'),
    ARGOCD('ARGOCD', 'argocd', 'argocd-slave')

    def name
    def executable
    def agentTag

    private DockerImage(Object name, Object executable, Object agentTag) {
        this.name = name
        this.executable = executable
        this.agentTag = agentTag
    }

}
