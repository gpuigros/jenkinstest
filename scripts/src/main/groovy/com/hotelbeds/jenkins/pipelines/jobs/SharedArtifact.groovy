package com.hotelbeds.jenkins.pipelines.jobs


class SharedArtifact {

    def jobName

    def artifacts


    SharedArtifact(Map config) {
        def sharedArtifact = config.entrySet().toList().first()
        this.jobName = sharedArtifact.key
        this.artifacts = sharedArtifact.value
    }

}
