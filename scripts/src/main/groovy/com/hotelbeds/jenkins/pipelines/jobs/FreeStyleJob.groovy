package com.hotelbeds.jenkins.pipelines.jobs

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureDefaultDatadogTags


/*Only for internal use in job templates*/
class FreeStyleJob extends Job {

    def command

    FreeStyleJob(Object config) {
        super(config)
    }


    @Override
    def build(dslFactory) {

        def dslJob = dslFactory.job(getJenkinsJobName())
        configureDefaultDatadogTags(this)
        dslJob = super.init(dslJob)

        dslJob.steps {
            shell(command)
        }

        super.finalize(dslJob)
    }
}
