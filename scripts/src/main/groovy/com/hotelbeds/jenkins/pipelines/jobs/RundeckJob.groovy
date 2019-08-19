package com.hotelbeds.jenkins.pipelines.jobs

import com.hotelbeds.jenkins.pipelines.Business
import com.hotelbeds.jenkins.pipelines.Profile
import com.hotelbeds.jenkins.pipelines.Provider
import com.hotelbeds.jenkins.pipelines.Region

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureDefaultDatadogTags
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureInjectGlobalPasswords
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.bashCommand

import com.hotelbeds.jenkins.Constants


class RundeckJob extends Job {


    static class RundeckCommand {

        def jobIdentifier

        def jobOptions

        RundeckCommand(config) {
            this.jobIdentifier = config.jobIdentifier
            this.jobOptions = config.jobOptions
        }

    }


    RundeckCommand command

    boolean tagDeployment = tagDeployment ?: false

    RundeckJob(Object config) {
        super(config)
    }

    def configureOnPremiseExecution() {
        this.profiles = Profile.ON_PREMISE.toString()
        this.jenkinsAgentTags = Profile.ON_PREMISE.toString()
    }


    @Override
    def build(dslFactory) {
        if (!isValid()) {
            throw new Error("${this.name} has validation errors")
        }

        def dslJob = dslFactory.job(getJenkinsJobName())

        configureOnPremiseExecution()
        configureDefaultDatadogTags(this)

        dslJob = super.init(dslJob)

        dslJob.configure { project ->
            project / 'publishers' / 'org.jenkinsci.plugins.rundeck.RundeckNotifier' {
                rundeckInstance('Default')
                jobId(this.command?.jobIdentifier)
                options(this.command?.jobOptions?.collect { item -> "$item" }?.join("\n"))
                shouldWaitForRundeckJob(true)
                shouldFailTheBuild(true)
                includeRundeckLogs(true)
                tailLog(true)
            }
        }

        dslJob.configure { project ->
            project.remove(project / 'steps')
        }

        if (this.tagDeployment) {
            dslJob.steps {
                shell(bashCommand('git-create-tag.sh', Constants.DEFAULT_PROJECT_DIR))
            }
        }

        super.finalize(dslJob)
    }
}
