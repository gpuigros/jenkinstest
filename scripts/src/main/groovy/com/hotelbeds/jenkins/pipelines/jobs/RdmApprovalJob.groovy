package com.hotelbeds.jenkins.pipelines.jobs

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureDefaultDatadogTags


class RdmApprovalJob extends Job {

    RdmApprovalJob(Object config) {
        super(config)
    }


    @Override
    def build(dslFactory) {
        if (!isValid()) {
            throw new Error("${this.name} has validation errors")
        }
        def dslJob = dslFactory.job(getJenkinsJobName())
        configureDefaultDatadogTags(this)
        dslJob = super.init(dslJob)
        super.finalize(dslJob)
    }
}
