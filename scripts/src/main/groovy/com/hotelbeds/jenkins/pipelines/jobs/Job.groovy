package com.hotelbeds.jenkins.pipelines.jobs

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureSharedArtifacts
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureStashRepo
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configurePipelineScriptsRepo
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureTrigger
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureDatadogTags
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureRdmJiraTicketPublish
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureInjectGlobalPasswords


import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Buildable
import com.hotelbeds.jenkins.pipelines.Validable
import com.hotelbeds.jenkins.pipelines.stages.Stage
import com.hotelbeds.jenkins.utils.Log
import com.hotelbeds.jenkins.pipelines.prbuilder.PullRequestBuilderAbstractJob


abstract class Job implements Validable, Buildable, Comparable {


    public static final LOG_ROTATOR_DAYS_TO_KEEP = 5

    public static final LOG_ROTATOR_NUM_TO_KEEP = 5

    public static final LOG_ROTATOR_ARTIFACTS_DAYS_TO_KEEP = 3

    public static final LOG_ROTATOR_ARTIFACTS_NUM_TO_KEEP = 3

    def firstJobOfPipeline = false

    def jenkinsAgentTags

    def manualTrigger = false

    def createRdmJiraTicket = false

    def rdmStandardChange

    String name

    def nexts = []

    def profiles = []

    def publishers

    ScmRepo scm

    boolean multiScmEnabled = true

    String scmRepoDirectory = Constants.DEFAULT_PROJECT_DIR

    SharedArtifact[] sharedArtifacts = []

    def showName

    Stage stage

    def individualJob = false

    def triggerParameters = ['BUILD_GIT_COMMIT': '${BUILD_GIT_COMMIT}', 'BUILD_VERSION': '${BUILD_VERSION}', 'RPM_RELEASE': '${RPM_RELEASE}',
                             'RPM_VERSION'     : '${RPM_VERSION}', 'VERSION': '${VERSION}']

    JobStatus triggerCondition = JobStatus.SUCCESS

    DatadogConfig datadog


    Job(config) {
        config.each { property, value ->
            if (property == datadog) {
                this."$property" = value
            }
            if (this.hasProperty(property)) {
                this."$property" = value
            }
        }
        Log.info(this.dump())
    }


    /**
     * Gets the jenkins job name.
     *
     * @return the jenkins job name
     */
    def getJenkinsJobName() {
        def name = "${this.stage?.pipeline?.jenkinsPath}/${this.stage?.pipeline?.name}_${this.name.toUpperCase().replaceAll(' ', '_')}".toString()
        return name
    }

    /**
     * Gets the jenkins job url.
     *
     * @return the jenkins job url
     */
    def getJenkinsJobUrl() {
        return "${Constants.JENKINS_URL}/job/${getJenkinsJobName().replaceAll('/', '/job/')}"
    }

    /**
     * Inits the DSL job.
     *
     * @param dslJob the dsl job
     * @return the java.lang. object
     */
    def init(dslJob) {

        cleanJob(dslJob)

        def pipelineBranch
        if (this.scm == null) {
            pipelineBranch = '${BUILD_GIT_COMMIT}'
        } else {
            pipelineBranch = this.scm.branch
        }

        if (this.firstJobOfPipeline) {
            def gitParamBranch = this.stage.pipeline.scmBranch.replaceAll('origin/', '')
            dslJob.parameters {
                stringParam('BUILD_GIT_COMMIT', '')
            }
        } else {
            dslJob.parameters {
                triggerParameters.each { triggerParameterKey, triggerParameterValue ->
                    stringParam(triggerParameterKey, '')
                }
            }
        }


        if (this.stage.name != PullRequestBuilderAbstractJob.PR_STAGE_NAME) {
            dslJob.wrappers {
                preScmSteps() {
                    steps {
                        shell('''echo "validating that BUILD_GIT_COMMIT parameter is not empty"
if [[ -z "${BUILD_GIT_COMMIT// }" ]]; then
        echo "BUILD_GIT_COMMIT is empty"
        exit 1
fi
''')
                    }
                    failOnError(true)
                }
            }
        }

        dslJob.label(jenkinsAgentTags)

        def pipelineRepository = this.scm == null ? this.stage.pipeline.scmRepository : this.scm.url


        if (multiScmEnabled) {
            dslJob.multiscm configurePipelineScriptsRepo() << configureStashRepo(pipelineRepository, pipelineBranch, scmRepoDirectory)
        } else {
            dslJob.scm configurePipelineScriptsRepo()
        }

        if (!this.individualJob) {
            dslJob.deliveryPipelineConfiguration(this.stage.name, this.showName)
        } else {
            dslJob.triggers { bitbucketPush() }
        }

        dslJob.deliveryPipelineConfiguration(this.stage.name, this.showName)
        dslJob.logRotator(LOG_ROTATOR_DAYS_TO_KEEP, LOG_ROTATOR_NUM_TO_KEEP, LOG_ROTATOR_ARTIFACTS_DAYS_TO_KEEP, LOG_ROTATOR_ARTIFACTS_NUM_TO_KEEP)
        dslJob.throttleConcurrentBuilds {
            maxPerNode(1)
            maxTotal(1)
            categories[this.stage.pipeline.name]
        }

        if (publishers?.junit != null) {
            dslJob.publishers {
                def reports = publishers.junit.join(', ')
                archiveJunit(reports) {
                    allowEmptyResults()
                }
            }
        }

        if (publishers?.testng != null) {
            dslJob.publishers {
                def reports = publishers.testng.join(', ')
                archiveTestNG(reports) {
                    showFailedBuildsInTrendGraph()
                }
            }
        }

        if (publishers?.allure != null) {
            dslJob.publishers {
                allure([Constants.DEFAULT_PROJECT_DIR + '/' + publishers.allure.path]) {
                    commandline(publishers.allure.version ? publishers.allure.version : 'allure1')
                    buildFor('ALWAYS')
                }
            }
        }

        if (publishers?.html != null) {
            dslJob.publishers {
                publishHtml {
                    report(Constants.DEFAULT_PROJECT_DIR + '/' + publishers.html.reportPath) {
                        reportFiles(publishers.html.reportFile)
                        reportName(publishers.html.reportTitle)
                    }
                }
            }
        }

        if (datadog?.tags != null) {
            dslJob.configure configureDatadogTags(this)
        }

        return dslJob
    }

    /**
     * Finalize the DSL job.
     *
     * @param dslJob the dsl job
     * @return the java.lang. object
     */
    def finalize(dslJob) {

        dslJob.configure configureSharedArtifacts(this)

        if (this.scm != null && this.scm.url.contains("-qa.git")) {
            //qa jobs generates allure reports, so we need to clean up the workspace to avoid high usage of disk.
            dslJob.publishers {
                wsCleanup {
                    deleteDirectories(true)
                    cleanWhenSuccess(true)
                    cleanWhenUnstable(true)
                    cleanWhenFailure(true)
                    cleanWhenNotBuilt(true)
                    cleanWhenAborted(true)
                    setFailBuild(false)
                }
            }
        } else {
            dslJob.publishers {
                wsCleanup {
                    deleteDirectories(true)
                    cleanWhenSuccess(true)
                    cleanWhenUnstable(false)
                    cleanWhenFailure(false)
                    cleanWhenNotBuilt(false)
                    cleanWhenAborted(false)
                    setFailBuild(false)
                }
            }
        }

        if (!this.stage.pipeline.recipients?.isEmpty()) {
            def jenkinsJobUrl = getJenkinsJobUrl()
            if (this.stage?.pipeline?.recipients != null) {
                dslJob.publishers {
                    extendedEmail {
                        recipientList(this.stage.pipeline.recipients)
                        defaultSubject("Jenkins - ${this.getJenkinsJobName()} is broken")
                        defaultContent("The execution \${BUILD_NUMBER} of job <a href='${jenkinsJobUrl}/\${BUILD_NUMBER}/console'>${jenkinsJobUrl}</a> has been failed. Please check it.")
                        contentType('text/html')
                        triggers {
                            failure {
                                sendTo {
                                    recipientList()
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!this.individualJob) {
            if (this.stage.pipeline.orchestrate) {
                if (!this.nexts.isEmpty()) {
                    dslJob.configure configureTrigger(this, this.nexts, this.triggerParameters)
                }
            } else {
                // Remove triggers
                dslJob.configure { project ->
                    project.remove(project / 'publishers' / 'au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger')
                    project.remove(project / 'publishers' / 'hudson.plugins.parameterizedtrigger.BuildTrigger' / 'configs' / 'hudson.plugins.parameterizedtrigger.BuildTriggerConfig')
                }
            }
        }

        if (createRdmJiraTicket) {
            def serviceName = this.stage?.pipeline?.name
            def nextStage = ""
            this.nexts.each { Job nextJob ->
                if (nextJob instanceof RdmApprovalJob) {
                    nextStage = nextJob.stage.name
                }
            }

            dslJob.configure configureInjectGlobalPasswords()

            //extendedEmail function will process the triggering condition
            dslJob.publishers configureRdmJiraTicketPublish(this, serviceName, nextStage, rdmStandardChange)

        }

        return dslJob
    }


    @Override
    boolean isValid() {
        return true
    }

    /**
     * Compare to.
     *
     * @param other the other
     * @return the int
     */
    @Override
    int compareTo(Object other) {
        this.name <=> other.name
    }

    /**
     * Clean job.
     *
     * @param dslJob the dsl job
     * @return the java.lang. object
     */
    def cleanJob(dslJob) {
        dslJob.configure { project ->
            project.remove(project / 'publishers')
        }
    }
}
