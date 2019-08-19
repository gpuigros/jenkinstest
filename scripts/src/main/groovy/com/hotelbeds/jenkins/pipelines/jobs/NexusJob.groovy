package com.hotelbeds.jenkins.pipelines.jobs

import com.hotelbeds.jenkins.pipelines.Profile

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureDefaultDatadogTags
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureImportedArtifacts
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureArtifactResolver
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.bashCommand
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.getScript

import com.hotelbeds.jenkins.pipelines.Technology
import com.hotelbeds.jenkins.Constants


class NexusJob extends Job {

    String nexusRepositoryId

    String environment

    boolean firstNexusJob


    NexusJob(Object config) {
        super(config)
        if (nexusRepositoryId != null) {
            multiScmEnabled = false
        }
    }


    @Override
    def build(dslFactory) {

        if (!isValid()) {
            throw new Error("${this.name} has validation errors")
        }

        def dslJob = dslFactory.job(getJenkinsJobName())
        configureDefaultDatadogTags(this)
        dslJob = super.init(dslJob)
        dslJob.configure configureImportedArtifacts(this)

        def rpmArchPreffix = "noarch"
        if (this.stage.pipeline.technology == Technology.ACE.code) {
            rpmArchPreffix = "x86_64"
        }
        def fileName = "${this.stage.pipeline.name}-\${BUILD_VERSION}.${rpmArchPreffix}.rpm"
        if (firstNexusJob) {
            if (this.stage.pipeline.sharedArtifacts.containsKey(this.name)) {
                if (this.stage.pipeline.packageType != "rpm") {
                    fileName = "${this.stage.pipeline.name}-\${BUILD_VERSION}.${this.stage.pipeline.packageType}"
                }
                dslJob.steps {
                    this.stage.pipeline.sharedArtifacts.get(this.name).each { fromJobName, artifacts ->
                        Job fromJob = this.stage.pipeline.findJobByName(fromJobName)
                        String artifactPrefixPath = Constants.DEFAULT_PROJECT_DIR + "/"
                        copyArtifacts(fromJob.getJenkinsJobName()) {
                            flatten(true)
                            includePatterns(artifactPrefixPath + artifacts.join().toString().replaceAll("\\[|\\]", "").split(",")*.trim().join(", " + artifactPrefixPath))
                            buildSelector { latestSuccessful(true) }
                        }
                    }
                }
            }

            if (this.stage.pipeline.packageType == "rpm") {
                if (this.stage.pipeline.technology == Technology.SPRING_BOOT.code) {
                    configureRpmValidation(dslJob, rpmArchPreffix)
                } else if (this.stage.pipeline.technology == Technology.ANGULAR_JS.code) {
                    configureRpmValidation(dslJob, rpmArchPreffix)
                } else if (this.stage.pipeline.technology == Technology.ACE.code) {
                    configureRpmValidation(dslJob, rpmArchPreffix)
                }
            }

        } else if (nexusRepositoryId != null) {
            fileName = "${this.stage.pipeline.name}-${this.stage.pipeline.packageType}-\${BUILD_VERSION}.${this.stage.pipeline.packageType}"
            String artifactId = "${this.stage.pipeline.name}-${this.stage.pipeline.packageType}"

            dslJob.configure configureArtifactResolver(Constants.DEFAULT_ARTIFACT_GROUP_ID, artifactId, '${BUILD_VERSION}', this.stage.pipeline.packageType)
        }

        def uploadArtifactCheckingScript = this.uploadArtifactCheckingScript(dslFactory)
        configureArtifactDeploy(dslJob, fileName, uploadArtifactCheckingScript, this.stage.pipeline.packageType)

        super.finalize(dslJob)
    }

    /**
     * Configure maven artifact deploy.
     *
     * @param dslJob the dsl job
     * @param fileName the file name
     * @param packaging the packaging
     * @return the java.lang. object
     */
    def configureArtifactDeploy(dslJob, fileName, checkUploadedArtifactScript, packaging) {
        def nexusUrl = getNexusRepositoryUrl()
        def mavenGoals = "--batch-mode deploy:deploy-file " +
                "-DgroupId=${Constants.DEFAULT_ARTIFACT_GROUP_ID} " +
                "-DartifactId=${this.stage.pipeline.name}-${packaging} " +
                "-Dversion=\${BUILD_VERSION} -Dfile=${fileName} " +
                "-DrepositoryId=hotelbeds-rpms -Durl=${nexusUrl} " +
                "-Dpackaging=${packaging} " +
                "-DgeneratePom=true -X -e"

        return configureMavenJobAsConditional(dslJob, mavenGoals, checkUploadedArtifactScript)
    }

    /**
     * Configure maven job as conditional.
     *
     * @param dslJob the dsl job
     * @param mavenGoals the maven goals
     * @param conditionalScript the conditional script
     * @return the java.lang. object
     */
    def configureMavenJobAsConditional(dslJob, mavenGoals, conditionalScript) {
        dslJob.steps {
            conditionalSteps {
                condition {
                    shell(conditionalScript)
                }
                runner('Run')
                steps {
                    maven {
                        mavenInstallation('maven-3')
                        goals(mavenGoals)
                        if (multiScmEnabled) {
                            rootPOM("${Constants.DEFAULT_PROJECT_DIR}/pom.xml")
                        }
                    }
                }
            }
        }
    }

    /**
     * Generate the nexus repository url
     * @return the nexus repository url
     */
    private String getNexusRepositoryUrl() {
        return "https://nexus-repository.${environment}.hotelbeds.com/nexus/content/repositories/${nexusRepositoryId}/"
    }

    /**
     * Configure rpm validation.
     *
     * @param dslJob the dsl job
     * @param archPreffix the arch preffix
     * @return the java.lang. object
     */
    def configureRpmValidation(dslJob, archPreffix) {
        if (nexusRepositoryId != null) {
            def rpmFile = "${this.stage.pipeline.name}-\${BUILD_VERSION}.${archPreffix}.rpm"
            dslJob.steps {
                shell(bashCommand('validate-rpm-file.sh', this.stage.pipeline.name, rpmFile))
            }
        }
    }

    /**
     * replace the variables needed and makes this script available from the workspace
     * @param dslFactory
     * @return nexus-check-artifact-already-deployed script
     */
    private String uploadArtifactCheckingScript(dslFactory) {
        def paramsMap = [
                "##environment##"      : environment,
                "##nexusRepositoryId##": nexusRepositoryId,
                "##artifactName##"     : this.stage.pipeline.name + "-" + this.stage.pipeline.packageType
        ]
        return getScript(dslFactory, 'nexus-check-artifact-already-deployed.sh', paramsMap)

    }

}
