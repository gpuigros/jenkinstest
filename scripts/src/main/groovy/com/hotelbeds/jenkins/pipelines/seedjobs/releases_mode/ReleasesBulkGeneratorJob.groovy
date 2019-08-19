package com.hotelbeds.jenkins.pipelines.seedjobs.releases_mode

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Buildable
import com.hotelbeds.jenkins.pipelines.Validable

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureAutoApprovalScriptlet

class ReleasesBulkGeneratorJob implements Buildable, Validable {

    String destinationPath

    ReleasesBulkGeneratorJob(destinationPath) {
        this.destinationPath = destinationPath
    }


    @Override
    def build(dslFactory) {
        dslFactory.folder(destinationPath) {
            displayName(destinationPath.split('/')?.last()?.toUpperCase())
        }

        def jobName = "${this.destinationPath}/buildblock-pipeline_BULK_GENERATOR"

        if (!isValid()) {
            throw new Error("$jobName has validation errors")
        }

        dslFactory.job(jobName) {
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
                    external(Constants.PIPELINE_BUILD_SCRIPTS_PATH + 'BulkRegenerator.groovy')
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
