package com.hotelbeds.jenkins.pipelines.jobs

import com.hotelbeds.jenkins.Constants

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.*

class AceJob extends Job {

    def command


    AceJob(Object config) {
        super(config)
    }


    @Override
    def build(dslFactory) {

        if (!isValid()) {
            throw new Error("${this.name} has validation errors")
        }

        def createVersionPropertiesScript = getScript(dslFactory, 'create-ace-version-properties.sh')
        createVersionPropertiesScript = createVersionPropertiesScript.replaceAll("##DEFAULT_PROJECT_DIR##", Constants.DEFAULT_PROJECT_DIR)

        def dslJob = dslFactory.job(getJenkinsJobName())
        configureDefaultDatadogTags(this)

        dslJob = super.init(dslJob)
        dslJob.configure configureBuildNameSetter(this.firstJobOfPipeline ? '${BUILD_VERSION}' : '#${BUILD_ID} for ${BUILD_VERSION}')
        dslJob.configure configureInjectGlobalPasswords()

        if (this.command == "build") {
            dslJob.steps {
                shell(createVersionPropertiesScript)
            }
            dslJob.configure configureUpdateBuildName(this)
            dslJob.configure configureEnvInjectBuilder(this)
            dslJob.steps {
                shell(bashCommand('ace-build.sh', Constants.DEFAULT_PROJECT_DIR))
            }
        } else if (this.command == "package") {
            dslJob.steps {
                shell(bashCommand('ace-package.sh', Constants.DEFAULT_PROJECT_DIR + "/"))
            }
        }


        super.finalize(dslJob)
    }

}
