package com.hotelbeds.jenkins.pipelines.jobs

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureBuildNameSetter
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureInjectGlobalPasswords
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureEnvInjectBuilder
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureUpdateBuildName
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.getScript
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.getJdkVersion
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureDefaultDatadogTags

import com.hotelbeds.jenkins.Constants


class MavenJob extends Job {

    static final String MAVEN_HELP_PLUGIN_VERSION = '3.0.0-SNAPSHOT'

    def command


    MavenJob(Object config) {
        super(config)
    }


    @Override
    def build(dslFactory) {

        if (!isValid()) {
            throw new Error("${this.name} has validation errors")
        }

        def createVersionPropertiesScript = getScript(dslFactory, 'create-version-properties.sh')
        createVersionPropertiesScript = createVersionPropertiesScript.replaceAll("##DEFAULT_PROJECT_DIR##", Constants.DEFAULT_PROJECT_DIR)

        def dslJob = dslFactory.mavenJob(getJenkinsJobName())
        configureDefaultDatadogTags(this)
        dslJob = super.init(dslJob)

        dslJob.configure configureBuildNameSetter(this.firstJobOfPipeline ? '${BUILD_VERSION}' : '#${BUILD_ID} for ${BUILD_VERSION}')
        dslJob.configure configureInjectGlobalPasswords()
        dslJob.archivingDisabled(true)
        dslJob.siteArchivingDisabled(true)
        dslJob.jdk(getJdkVersion(this.stage.pipeline.jdkVersion))

        if (this.firstJobOfPipeline) {
            dslJob.preBuildSteps {
                maven {
                    mavenInstallation('maven-3')
                    goals("org.apache.maven.plugins:maven-help-plugin:${MAVEN_HELP_PLUGIN_VERSION}:evaluate -Dexpression=\"project.version\" -Doutput=version.properties")
                    rootPOM("${Constants.DEFAULT_PROJECT_DIR}/pom.xml")
                }
                shell(createVersionPropertiesScript)
            }
            dslJob.configure configureUpdateBuildName(this)
            dslJob.configure configureEnvInjectBuilder(this)

        }

        if (this.stage.pipeline.sharedArtifacts.containsKey(this.name)) {
            dslJob.preBuildSteps {
                this.stage.pipeline.sharedArtifacts.get(this.name).each { fromJobName, artifacts ->
                    Job fromJob = this.stage.pipeline.findJobByName(fromJobName)
                    String artifactPrefixPath = Constants.DEFAULT_PROJECT_DIR + "/"
                    copyArtifacts(fromJob.getJenkinsJobName()) {
                        includePatterns(artifactPrefixPath + artifacts.join().toString().replaceAll("\\[|\\]", "").split(",")*.trim().join(", " + artifactPrefixPath))
                        buildSelector { latestSuccessful(false) }
                    }
                }
            }
        }
        if (this.command instanceof String) {
            dslJob.goals(this.command)
            dslJob.rootPOM("${Constants.DEFAULT_PROJECT_DIR}/pom.xml")
        } else if (this.command instanceof ArrayList) {
            dslJob.goals('clean')
            dslJob.rootPOM("${Constants.DEFAULT_PROJECT_DIR}/pom.xml")
            dslJob.postBuildSteps {
                this.command.each { command ->
                    maven {
                        mavenInstallation('maven-3')
                        goals(command)
                        rootPOM("${Constants.DEFAULT_PROJECT_DIR}/pom.xml")
                    }
                }
            }
        }

        super.finalize(dslJob)
    }
}
