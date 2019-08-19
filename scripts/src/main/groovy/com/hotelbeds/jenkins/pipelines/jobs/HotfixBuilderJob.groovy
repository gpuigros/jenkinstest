package com.hotelbeds.jenkins.pipelines.jobs

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Buildable
import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.pipelines.Validable


class HotfixBuilderJob implements Buildable, Validable {

    Pipeline pipeline


    HotfixBuilderJob(pipeline) {
        this.pipeline = pipeline
    }


    @Override
    def build(dslFactory) {

        def jobName = "${this.pipeline?.name}_HOTFIX_BUILDER"
        def jenkinsJobName = "${this.pipeline?.jenkinsPath}/$jobName"

        if (!isValid()) {
            throw new Error("$jenkinsJobName has validation errors")
        }

        dslFactory.job(jenkinsJobName) {
            parameters {
                gitParam('BUILD_BRANCH') {
                    sortMode('DESCENDING')
                    description('Branch to be used to launch the pipeline.')
                    type('BRANCH')
                    defaultValue('origin/master')
                }
            }

            scm {
                git {
                    remote {
                        url(this.pipeline?.scmRepository)
                        credentials(Constants.SSH_KEY_ID)
                    }
                    branch("*/master")
                    extensions { gitRepo -> relativeTargetDirectory('project') }
                }
            }

            publishers {
                downstreamParameterized {
                    trigger("${jobName.replaceAll('HOTFIX_BUILDER', 'BUILD')}") {
                        condition('SUCCESS')
                        parameters {
                            predefinedProp('BUILD_GIT_COMMIT', '${BUILD_BRANCH}')
                        }
                    }
                }
            }
        }

    }


    @Override
    boolean isValid() {
        return true
    }
}
