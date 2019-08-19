package com.hotelbeds.jenkins.pipelines.jobs

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.jobs.tech_versions.Python

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.*

class PythonJob extends Job {

    def command


    PythonJob(Object config) {
        super(config)
    }


    @Override
    def build(dslFactory) {

        if (!isValid()) {
            throw new Error("${this.name} has validation errors")
        }

        Python pythonVersion = getPythonVersion(this.stage.pipeline.pythonVersion)

        String pipelineName = this.stage.pipeline.name
        def createVersionPropertiesScript = getScript(dslFactory, 'create-python-version-properties.sh')
        createVersionPropertiesScript = createVersionPropertiesScript.replaceAll("##DEFAULT_PROJECT_DIR##", Constants.DEFAULT_PROJECT_DIR)

        def dslJob = dslFactory.job(getJenkinsJobName())
        configureDefaultDatadogTags(this)
        dslJob = super.init(dslJob)

        dslJob.configure configureBuildNameSetter(this.firstJobOfPipeline ? '${BUILD_VERSION}' : '#${BUILD_ID} for ${BUILD_VERSION}')
        dslJob.configure configureInjectGlobalPasswords()

        if (this.firstJobOfPipeline) {
            dslJob.steps {
                shell(createVersionPropertiesScript)
            }
            dslJob.configure configureUpdateBuildName(this)
            dslJob.configure configureEnvInjectBuilder(this)
        }

        if (command.contains("docker_push")) {
            dslJob.steps configureVirtualEnvPythonAWSCli(pipelineName, pythonVersion)
        }

        if (command.contains("setuptools_hbg.command")) {
            dslJob.steps configureVirtualEnvPythonHbgSetuptools(pipelineName, pythonVersion)
        }

        dslJob.steps configureVirtualEnvPythonStep(command, pipelineName, pythonVersion)

        if (command.contains("package_lambda_code")) {
            dslJob.steps {
                shell('cp ' + Constants.DEFAULT_PROJECT_DIR + '/serverless* ' + Constants.DEFAULT_PROJECT_DIR + '/target '
                        + '&& cd ' + Constants.DEFAULT_PROJECT_DIR + '/target && zip -r ' + pipelineName + '-${BUILD_VERSION}.zip *.zip serverless*.yml')
            }
        }

        super.finalize(dslJob)
    }
}
