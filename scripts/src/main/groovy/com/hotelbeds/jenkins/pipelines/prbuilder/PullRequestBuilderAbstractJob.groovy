package com.hotelbeds.jenkins.pipelines.prbuilder

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.pipelines.jobs.Job

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureAutoApprovalScriptlet
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureBuildNameSetter
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureDefaultDatadogTags
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureInjectGlobalPasswords
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.getJdkVersion
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.getScript

abstract class PullRequestBuilderAbstractJob extends Job {

    public static final String PR_STAGE_NAME = 'PR BUILDER'
    static final String DEFAULT_SPRING_PROFILES = 'dev,ci'
    static final String DEFAULT_JENKINS_AGENT_TAG = 'ON_PREMISE'
    def dslJob
    def dslFactory
    def configureGitScript


    PullRequestBuilderAbstractJob(Pipeline pipeline) {
        super(new String[0])
        this.firstJobOfPipeline = false
        this.jenkinsAgentTags = DEFAULT_JENKINS_AGENT_TAG
        this.profiles = pipeline.pullRequestBuilder.profiles
        if (this.profiles == null) {
            this.profiles = DEFAULT_SPRING_PROFILES
        }
    }


    @Override
    def build(dslFactory) {
        this.stage.jobs.add(this)

        if (!isValid()) {
            throw new Error("${this.name} has validation errors")
        }

        this.dslFactory = dslFactory
        dslJob = dslFactory.job(getJenkinsJobName())
        configureDefaultDatadogTags(this)

        dslJob = super.init(dslJob)
        dslJob.scm configureGitRepo(this.scm.url, this.scm.branch)
        dslJob.configure configureInjectGlobalPasswords()
        dslJob.jdk(getJdkVersion(this.stage.pipeline.jdkVersion))

        configureGitScript = getScript(dslFactory, 'git-configure.sh')
        customBuild()

        dslJob.configure configureBuildNameSetter('#${BUILD_NUMBER} - PR#${PULL_REQUEST_ID}')

        dslJob.configure configureAutoApprovalScriptlet()

        if (this.nexts.isEmpty()) {
            dslJob.configure configureBitbucketNotifierPlugin()
        }

        super.finalize(dslJob)
    }


    def customBuild() {

    }

    /**
     * Configure bitbucket notifier plugin.
     *
     * @return the closure
     */
    static Closure configureBitbucketNotifierPlugin() {
        return { project ->
            project / 'publishers' / 'org.jenkinsci.plugins.stashNotifier.StashNotifier' {
                stashServerBaseUrl()
                credentialsId()
                ignoreUnverifiedSSLPeer(false)
                commitSha1()
                includeBuildNumberInKey(false)
                projectKey()
                prependParentProjectKey(false)
                disableInprogressNotification(false)
            }
        }
    }


    /**
     * Configure git repo.
     *
     * @param repoUrl the repo url
     * @param repoBranch the repo branch
     * @return the closure
     */
    static Closure configureGitRepo(String repoUrl, String repoBranch) {
        return {
            git {
                remote {
                    url(repoUrl)
                    credentials(Constants.SSH_KEY_ID)
                }
                branch(repoBranch)
                extensions {
                    cleanBeforeCheckout()
                    relativeTargetDirectory(Constants.DEFAULT_PROJECT_DIR)
                    mergeOptions {
                        remote('origin')
                        branch('master')
                        strategy('default')
                        fastForwardMode(FastForwardMergeMode.FF)
                    }
                }
            }
        }
    }

}
