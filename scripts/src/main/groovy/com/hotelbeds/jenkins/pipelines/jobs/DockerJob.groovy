package com.hotelbeds.jenkins.pipelines.jobs

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.jobs.tech_versions.DockerImage

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.*

class DockerJob extends Job {

    def image

    def parameters

    def awsProfile

    def extraParams

    //Use to add something before executable if needed.
    def execPreffix = ""

    DockerJob(Object config) {
        super(config)
        //docker jobs should not need clone pipeline repository
        multiScmEnabled = false

        //extraParams would be generated for each type of docker image
        extraParams = ""
    }


    @Override
    def build(dslFactory) {

        if (!isValid()) {
            throw new Error("${this.name} has validation errors")
        }

        DockerImage dockerImage = getDockerImage()

        this.jenkinsAgentTags = dockerImage.agentTag

        def dslJob = dslFactory.job(getJenkinsJobName())
        configureDefaultDatadogTags(this)
        dslJob = super.init(dslJob)

        configureCustomSteps(dslJob)

        if (execPreffix != "") {
            execPreffix += " "
        }

        //Common steps for all docker images. This must be generic
        dslJob.steps {
            shell(execPreffix + dockerImage.executable + " " + this.parameters?.collect { item -> "$item" }?.join(" ") + extraParams )
        }

        super.finalize(dslJob)
    }

    private DockerImage getDockerImage() {
        DockerImage dockerImage
        try {
            dockerImage = DockerImage.valueOf(this.image)
        } catch (IllegalArgumentException e) {
            throw new Error("${this.image} is not a valid docker image name")
        }
        return dockerImage
    }


    def configureCustomSteps(dslJob) {
        switch (image) {
            case DockerImage.SERVERLESS.name:
                configureStepsForServerlessImage(dslJob)
                break
            case DockerImage.ARGOCD.name:
                configureStepsForArgocdImage(dslJob)
                break
        }
    }


    /**
     * Configure steps for serverless image
     * @param dslJob
     * @return
     */
    def configureStepsForServerlessImage(dslJob) {
        processServerlessExtraParams()
        String artifactId = "${this.stage.pipeline.name}-${this.stage.pipeline.packageType}"

        dslJob.configure configureArtifactResolver(Constants.DEFAULT_ARTIFACT_GROUP_ID, artifactId, '${BUILD_VERSION}', this.stage.pipeline.packageType)

        dslJob.steps {
            shell("unzip *.zip")
        }
    }

    /**
     * Configure steps for ArgoCD image
     * @param dslJob
     * @return
     */
    def configureStepsForArgocdImage(dslJob) {
        execPreffix = "USER=argocd"

        dslJob.steps {
            shell(execPreffix + " " + DockerImage.ARGOCD.executable + ' login ${ARGOCD_SERVER} --username admin --password ${ARGOCD_ADMIN_CREDENTIAL} --insecure')
        }
    }

    def processServerlessExtraParams() {
        def awsProfileParts = awsProfile.split('-')

        addExtraParam("--aws-profile ${awsProfile}")
        addExtraParam("--onsom-environment ${awsProfileParts[0]}")
        addExtraParam("--onsom-business ${awsProfileParts[1]}")
        addExtraParam("--onsom-provider ${awsProfileParts[2]}")
        addExtraParam("--onsom-region ${awsProfileParts[(3..-1)].join('-')}")
    }


    def addExtraParam(def parameter) {
        extraParams += " " + parameter
    }

}
