package com.hotelbeds.jenkins.pipelines

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.jobs.Job
import com.hotelbeds.jenkins.pipelines.jobs.JobTemplate
import com.hotelbeds.jenkins.pipelines.jobs.JobType
import com.hotelbeds.jenkins.pipelines.jobs.JobFactory
import com.hotelbeds.jenkins.pipelines.jobs.NexusJob
import com.hotelbeds.jenkins.pipelines.prbuilder.PullRequestBuilder
import com.hotelbeds.jenkins.pipelines.prbuilder.PullRequestBuilderJob
import com.hotelbeds.jenkins.pipelines.prbuilder.PullRequestAnalysisJob
import com.hotelbeds.jenkins.pipelines.jobs.RdmApprovalJob
import com.hotelbeds.jenkins.pipelines.jobs.SharedArtifact

import com.hotelbeds.jenkins.pipelines.stages.Stage


class Pipeline implements Buildable {

    def jenkinsPath

    def jobsFlows = [:].withDefault { [] }

    String name

    def profiles = []

    def orchestrate = true

    String scmBranch

    String scmRepository

    def sharedArtifacts = [:].withDefault { [:].withDefault { [] } }

    def stages = []

    def configRepos = []

    def technology

    def recipients

    def releasementStrategy = Constants.CONTINUOUS_DEPLOYMENT_RELEASEMENT_STRATEGY

    def generateRdmTicket = false

    def rdmStandardChange = false

    def scmCron = scmCron ?: 'H/5 * * * *'

    def builtByVersion

    def jdkVersion

    def pythonVersion

    def nodejsVersion

    String packageType = "rpm"

    PullRequestBuilder pullRequestBuilder


    Pipeline(config) {
        config.each { property, value ->
            if (this.hasProperty(property) && property != 'stages') {
                println "$property = $value"
                this."$property" = value
            }
        }

        Stage previousStage = null
        config.stages.each { stageConfig ->
            processJobTemplates(stageConfig)
            Stage stage = new Stage(stageConfig)
            stage.pipeline = this
            stages.push(stage)
            addJobsFlow(stage.jobsFlows)
            previousStage = stage
        }

        buildJobsFlow()

        if (stages && stages.size() > 0) {
            stages.first()?.jobs?.first()?.firstJobOfPipeline = true
        }

        profiles?.each { profile ->
            def jobs = findJobsByTypeAndProfile(NexusJob.class, profile)
            if (jobs?.size() > 0) {
                jobs?.first()?.firstNexusJob = true
            }
        }
    }

    /**
     * Usage example:
     *             - type: template
     *             profiles: ON_PREMISE
     *             template: ARGOCD
     *             parameters:
     *               environment: test
     *
     *
     * @param stageConfig - The complete stage configuration before parsing to pipeline objects.
     *
     */
    def processJobTemplates(stageConfig) {

        def jobTemplates = []

        def templates = 0
        //Identify the job template into that stage
        stageConfig.jobs.eachWithIndex { jobConfig, index ->
            if (jobConfig.type == JobType.TEMPLATE.code()) {
                templates++
                jobTemplates.addAll(new JobTemplate(index, jobConfig.template, jobConfig.parameters))
            }
        }

        def advanceIndex = 0
        if (jobTemplates.size() > 0) {
            jobTemplates.each { jobTemplate ->
                jobTemplate.templateIndex += advanceIndex
                jobTemplate.configure(this, stageConfig)
                advanceIndex = jobTemplate.templateHelper.templates.size() - 1 //+ new real jobs - template
            }
        } else if (templates > 0) {
            throw new Error("There are JobTemplates not processed")
        }

    }

    /**
     * Builds the jobs flow.
     *
     * @return the java.lang. object
     */
    def buildJobsFlow() {
        this.jobsFlows.each { profile, jobsFlow ->
            Job previousJob = null
            jobsFlow.collect { job ->
                if (previousJob && !previousJob.nexts.contains(job)) {
                    previousJob.nexts << job
                }
                if (job instanceof RdmApprovalJob) {
                    job.manualTrigger = true
                }
                previousJob = job
            }
        }
    }

    /**
     * Adds the jobs flow.
     *
     * @param stageJobsFlow the stage jobs flow
     * @return the java.lang. object
     */
    def addJobsFlow(stageJobsFlow) {
        stageJobsFlow.each { profile, jobs ->
            this.jobsFlows.get(profile).addAll(jobs)
        }
    }


    @Override
    def build(dslFactory) {
        stages.each { Stage stage -> stage.build(dslFactory) }
        println "111"
        if (this.orchestrate) {
            generatePipelineView(dslFactory)
        }
        println "222"
        if (this.pullRequestBuilder?.mode != null) {
            generatePullRequestBuilderView(dslFactory)
        }
        println "333"
        if (!this.configRepos.isEmpty()) {
            configureDeployConfigRepositoriesToS3(dslFactory)
        }
    }

    def generatePipelineView(dslFactory) {
        dslFactory.deliveryPipelineView(getJenkinsPipelineName()) {
            pipelineInstances(5)
            showAggregatedPipeline(false)
            columns(2)
            configure { view ->
                view / 'se.diabol.jenkins.pipeline.DeliveryPipelineView' { showTestResults(true) }
            }
            allowRebuild()
            allowPipelineStart()
            sorting(Sorting.TITLE)
            enableManualTriggers()
            showAvatars()
            showDescription()
            showPromotions()
            showChangeLog()
            showTestResults()
            updateInterval(60)
            pipelines { component(stages?.first().name, (stages?.first()?.jobs?.first() as Job).getJenkinsJobName()) }
        }
    }


    def generatePullRequestBuilderView(dslFactory) {
        PullRequestBuilderJob prCheckerJob = new PullRequestBuilderJob(this)
        if (this.pullRequestBuilder.codeAnalysis) {
            PullRequestAnalysisJob prAnalysisJob = new PullRequestAnalysisJob(this)
            prCheckerJob.stage.jobs.add(prCheckerJob) //To manually configure jobs workflow
            prAnalysisJob.build(dslFactory) //Must be build before build prCheckerJob
            prCheckerJob.nexts.add(prAnalysisJob)
        }
        prCheckerJob.build(dslFactory)

        dslFactory.deliveryPipelineView("${this.jenkinsPath}/Σ_PR") {
            pipelineInstances(5)
            showAggregatedPipeline(false)
            columns(2)
            configure { view ->
                view / 'se.diabol.jenkins.pipeline.DeliveryPipelineView' { showTestResults(true) }
            }
            allowRebuild()
            allowPipelineStart()
            sorting(Sorting.TITLE)
            enableManualTriggers()
            showAvatars()
            showDescription()
            showPromotions()
            showChangeLog()
            showTestResults()
            updateInterval(60)
            pipelines { component(prCheckerJob.stage.name, prCheckerJob.getJenkinsJobName()) }
        }
    }

    /**
     * Configure shared artifacts.
     *
     * @return the java.lang. object
     */
    def configureSharedArtifacts() {
        this.stages.each { Stage stage ->
            stage.jobs.each { Job job ->
                job.sharedArtifacts.each { SharedArtifact sharedArtifact ->
                    this.sharedArtifacts.get(sharedArtifact.jobName).get(job.name) << sharedArtifact.artifacts
                }
            }
        }
    }



    def configureRdmNotifications() {
        if (this.generateRdmTicket) {
            this.stages.each { Stage stage ->
                stage.jobs.each { Job job ->
                    if (!job.nexts.isEmpty()) {
                        job.nexts.each { Job nextJob ->
                            if (nextJob instanceof RdmApprovalJob) {
                                job.createRdmJiraTicket = true
                                job.rdmStandardChange = rdmStandardChange
                            }
                        }
                    }
                }
            }
        }
    }


    def configureDeployConfigRepositoriesToS3(dslFactory) {

        if (!this.configRepos.isEmpty()) {

            this.configRepos.each { configRepo ->

                configRepo.stage = [
                        pipeline: [
                                jenkinsPath: this.jenkinsPath,
                                name       : this.name,
                                recipients : this.recipients
                        ]
                ]

                configRepo.scm = [
                        url   : configRepo.repositoryUrl,
                        branch: "master"
                ]

                configRepo.type = JobType.RUNDECK.code()
                configRepo.individualJob = true

                configRepo.command = [
                        jobIdentifier: '92572260-5169-4638-b90d-490fafdde5ad',
                        jobOptions   : [
                                "environment=" + configRepo.environment,
                                "git_repository_url=" + configRepo.repositoryUrl,
                                "s3_bucket_name=" + configRepo.s3bucket,
                        ]
                ]

                Job job = JobFactory.getInstance(configRepo)

                job.build(dslFactory)

            }
        }

    }

    /**
     * Find job by name.
     *
     * @param name the name
     * @return the java.lang. object
     */
    def findJobByName(String name) {
        for (Stage stage in this.stages) {
            for (Job job in stage.jobs) {
                if (job.name == name) {
                    return job
                }
            }
        }
        return null
    }

    /**
     * Find jobs by type.
     *
     * @param type the type
     * @return the java.lang. object
     */
    def findJobsByType(Class<?> type) {
        def typeJobs = []
        for (Stage stage in this.stages) {
            for (Job job in stage.jobs) {
                if (job in type) {
                    typeJobs << job
                }
            }
        }
        return typeJobs
    }

    /**
     * Find jobs by type and profile.
     *
     * @param type the type
     * @param profile the profile
     * @return the java.lang. object
     */
    def findJobsByTypeAndProfile(Class<?> type, String profile) {
        def typeJobs = []
        for (Stage stage in this.stages) {
            for (Job job in stage.jobs) {
                if (job in type && profile in job.profiles) {
                    typeJobs << job
                }
            }
        }
        return typeJobs
    }

    /**
     * Gets the jenkins pipeline name.
     *
     * @return the jenkins pipeline name
     */
    def getJenkinsPipelineName() {
        def name = "${this.jenkinsPath}/${this.name}"
        return name
    }
}
