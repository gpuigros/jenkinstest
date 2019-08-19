package com.hotelbeds.jenkins.pipelines.seedjobs

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Buildable
import com.hotelbeds.jenkins.pipelines.Validable

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureAutoApprovalScriptlet
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureExtendedChoiceParameter
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.getScript


class GeneratorJob implements Buildable, Validable {

    String destinationPath

    GeneratorJob(destinationPath) {
        this.destinationPath = destinationPath
    }


    @Override
    def build(dslFactory) {
        dslFactory.folder(destinationPath) {
            displayName(destinationPath.split('/')?.last()?.toUpperCase())
        }

        def jobName = "${this.destinationPath}/buildblock-pipeline_GENERATOR"

        if (!isValid()) {
            throw new Error("$jobName has validation errors")
        }

        def listPipelinesScript = getScript(dslFactory, 'list-pipelines.groovy')

        dslFactory.job(jobName) {
            configure configureExtendedChoiceParameter('buildblock-pipeline_GENERATOR', 'PIPELINE', 'PT_SINGLE_SELECT', listPipelinesScript, ',', 1000)
            label('master')
            multiscm {
                git {
                    remote {
                        url(Constants.PIPELINES_SCRIPTS_REPO)
                        credentials(Constants.SSH_KEY_ID)
                    }
                    branch(Constants.PIPELINES_SCRIPTS_BRANCH)
                    extensions { relativeTargetDirectory(Constants.DEFAULT_SCRIPTS_DIR) }
                }
                git {
                    remote {
                        url(Constants.PIPELINES_REPO)
                        credentials(Constants.SSH_KEY_ID)
                    }
                    branch('*/master')
                    extensions {
                        relativeTargetDirectory(Constants.DEFAULT_PIPELINES_DIR)
                        localBranch('master')
                    }
                }
            }
            steps {
                dsl {
                    external(Constants.PIPELINE_BUILD_SCRIPTS_PATH + 'CreateRegenerator.groovy')
                    lookupStrategy('JENKINS_ROOT')
                    additionalClasspath('scripts/lib/*.jar')
                }
                dsl {
                    external(Constants.PIPELINE_BUILD_SCRIPTS_PATH + 'CreateHotfixBuilder.groovy')
                    lookupStrategy('JENKINS_ROOT')
                    additionalClasspath('scripts/lib/*.jar')
                }
            }
            configure configureAutoApprovalScriptlet()
        }
    }


    @Override
    boolean isValid() {
        return true
    }
}
