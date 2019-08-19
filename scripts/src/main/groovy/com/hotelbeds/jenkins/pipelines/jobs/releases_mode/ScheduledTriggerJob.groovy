package com.hotelbeds.jenkins.pipelines.jobs.releases_mode


import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Buildable
import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.pipelines.Validable


class ScheduledTriggerJob implements Buildable, Validable {

    Pipeline pipeline

    String pollingBranch


    ScheduledTriggerJob(pipeline, branch) {
        this.pipeline = pipeline
        this.pollingBranch = branch
    }


    @Override
    def build(dslFactory) {
        def jobName = "${this.pipeline?.name}_SCHEDULED_TRIGGER"
        def jenkinsJobName = "${this.pipeline?.jenkinsPath}/$jobName"

        if (!isValid()) {
            throw new Error("$jenkinsJobName has validation errors")
        }


        dslFactory.job(jenkinsJobName) {

            scm {
                git {
                    remote {
                        url(this.pipeline?.scmRepository)
                        credentials(Constants.SSH_KEY_ID)
                    }
                    branch("${this.pollingBranch}")
                    extensions { gitRepo -> relativeTargetDirectory('project') }
                }
            }

            triggers { bitbucketPush() }

            publishers {
                downstreamParameterized {
                    trigger("${jobName.replaceAll('SCHEDULED_TRIGGER', 'REGENERATOR')}") {
                        condition('SUCCESS')
                        parameters {
                            predefinedProp('DEPLOYMENT_DEFAULT_BRANCH', 'false')
                            predefinedProp('SCM_BRANCH', this.pollingBranch)
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
