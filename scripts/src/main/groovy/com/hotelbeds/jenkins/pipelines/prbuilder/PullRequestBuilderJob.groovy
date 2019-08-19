package com.hotelbeds.jenkins.pipelines.prbuilder

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.pipelines.Technology
import com.hotelbeds.jenkins.pipelines.jobs.ScmRepo
import com.hotelbeds.jenkins.pipelines.jobs.SharedArtifact
import com.hotelbeds.jenkins.pipelines.stages.Stage

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.getScript


class PullRequestBuilderJob extends PullRequestBuilderAbstractJob {

    static final String JOB_NAME = 'PR_BUILDER'

    static final String[] SHARED_ARTIFACTS = ['**/target/coverage-reports/jacoco-ut.exec',
                                              '**/target/surefire-reports/*.xml',
                                              '**/target/classes/**/*.class']

    static final String JUNIT_REPORTS_PATH = '**/target/surefire-reports/*.xml, **/target/failsafe-reports/*.xml'

    def buildCommand


    PullRequestBuilderJob(Pipeline pipeline) {
        super(pipeline)
        Map stageConfig = new HashMap<String, Object>()
        stageConfig.put("name", PR_STAGE_NAME)
        stageConfig.put("pipeline", pipeline)

        Map scmRepoConfig = new HashMap<String, String>()
        scmRepoConfig.put("url", pipeline.scmRepository)
        scmRepoConfig.put("branch", 'origin/pr/master')

        Map sharedArtifactsConfig = new HashMap<String, Object>()
        sharedArtifactsConfig.put(JOB_NAME, SHARED_ARTIFACTS)

        this.triggerParameters = ['PULL_REQUEST_URL': '$PULL_REQUEST_URL',
                                  'PULL_REQUEST_ID' : '$PULL_REQUEST_ID',
                                  'BUILD_GIT_COMMIT': '${GIT_NOTIF_COMMIT}']
        this.name = JOB_NAME
        this.stage = new Stage(stageConfig)
        this.scm = new ScmRepo(scmRepoConfig)
        this.sharedArtifacts = new SharedArtifact(sharedArtifactsConfig)

        /* Necessary so that the next job can recover the shared artifacts.
        By not going through the normal pipeline construction workflow,
        it must be included in the pipeline level array.
         */
        this.stage.pipeline.stages.add(this.stage)
        Map sharedArtifactsPipelineConfig = new HashMap<String, Object>()
        sharedArtifactsPipelineConfig.put(PullRequestAnalysisJob.JOB_NAME, sharedArtifactsConfig)
        this.stage.pipeline.sharedArtifacts.putAll(sharedArtifactsPipelineConfig)

        setTechnologyConfigurationForBuilderMode(pipeline)
    }


    def setTechnologyConfigurationForBuilderMode(Pipeline pipeline) {
        switch (pipeline.technology) {
            case Technology.SPRING_BOOT.code:
                setJavaCommandForBuilderMode(pipeline)
                break
            default:
                break
        }
    }


    @Override
    def customBuild() {

        def setPrBuilderJobDescriptionScript = getScript(dslFactory, 'set-pr-checker-job-description.groovy')
        def getCommitNotifHashScript = getScript(dslFactory, 'get-commit-notif-hash.groovy')
        def validatePRParamsScript = getScript(dslFactory, 'pr-validate-params.sh')

        dslJob.triggers { scm('H 0 1 1 0') }

        setTechnologySteps()

        dslJob.wrappers {
            preScmSteps() {
                steps {
                    systemGroovyCommand(setPrBuilderJobDescriptionScript)
                    shell(configureGitScript)
                    shell(validatePRParamsScript)
                }
                failOnError(false)
            }
        }

        dslJob.publishers {
            archiveJunit(JUNIT_REPORTS_PATH) {
                allowEmptyResults()
            }

            groovyPostBuild(getCommitNotifHashScript, Behavior.MarkFailed)
        }

    }

    def setTechnologySteps() {
        switch (this.stage.pipeline.technology) {
            case Technology.SPRING_BOOT.code:
                dslJob.steps setJavaSteps()
                break
            default:
                break
        }
    }


    /************************* TECHNOLOGY JAVA ****************/

    private void setJavaCommandForBuilderMode(Pipeline pipeline) {
        switch (pipeline.pullRequestBuilder.mode) {
            case PullRequestBuilderMode.SECURE:
                this.buildCommand = 'clean verify -Dspring.profiles.active=' + this.profiles + ' -X -e -U'
                this.showName = 'VERIFY'
                break
            case PullRequestBuilderMode.NINJA:
                this.buildCommand = 'clean test -X -e -U'
                this.showName = 'TEST'
                break
            case PullRequestBuilderMode.KAMIKAZE_MONKEY:
                this.buildCommand = 'clean compile -X -e -U'
                this.showName = 'COMPILE'
                break
        }
    }

    Closure setJavaSteps() {
        return {
            maven {
                mavenInstallation('maven-3')
                goals(this.buildCommand)
                rootPOM("${Constants.DEFAULT_PROJECT_DIR}/pom.xml")
            }
        }
    }


}
