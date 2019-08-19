package com.hotelbeds.jenkins.pipelines.stages

import com.hotelbeds.jenkins.pipelines.Buildable
import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.pipelines.jobs.Job
import com.hotelbeds.jenkins.pipelines.jobs.JobFactory


class Stage implements Buildable {

    def jobs = []

    def jobsFlows = [:].withDefault { [] }

    String name

    Pipeline pipeline


    Stage(config) {
        config.each { property, value ->
            if (this.hasProperty(property) && property != 'jobs') {
                this."$property" = value
            }
        }
        config.jobs.each { jobConfig ->
            Job job = JobFactory.getInstance(jobConfig)

            if (job == null) {
                throw new Error(jobConfig.toString())
            }

            job.stage = this
            jobs.push(job)
            def profiles = []
            profiles.addAll(job.profiles)
            profiles.each { profile ->
                jobsFlows.get(profile) << job
            }
        }
    }


    @Override
    def build(dslFactory) {
        jobs.each { Job job -> job.build(dslFactory) }
    }

}
