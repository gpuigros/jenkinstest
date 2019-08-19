package com.hotelbeds.jenkins.pipelines.regenerator

import com.hotelbeds.jenkins.Constants

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.*

class RegeneratorContinuousDeploymentStrategyJob extends RegeneratorJob {


    RegeneratorContinuousDeploymentStrategyJob(pipeline, pipelineFileName) {
        super(pipeline, pipelineFileName)
    }


    @Override
    def build(dslFactory) {

        def dslJob = init(dslFactory)

        dslJob.configure configureDatadogTagsForRegenerator(this.pipeline.name)

        dslJob.triggers { bitbucketPush() }

        dslJob.multiscm {
            git {
                remote {
                    url(this.pipeline?.scmRepository)
                    credentials(Constants.SSH_KEY_ID)
                }

                branch(this.pipeline.scmBranch)

                extensions { gitRepo -> relativeTargetDirectory(Constants.DEFAULT_PROJECT_DIR) }
                configure { node ->
                    node / 'extensions' / 'hudson.plugins.git.extensions.impl.PathRestriction' { excludedRegions('helm/.*') }
                }
            }
            git {
                remote {
                    url(Constants.PIPELINES_SCRIPTS_REPO)
                    credentials(Constants.SSH_KEY_ID)
                }
                branch(Constants.PIPELINES_SCRIPTS_BRANCH)
                extensions { relativeTargetDirectory(Constants.DEFAULT_SCRIPTS_DIR) }
                configure { node ->
                    node / 'extensions' / 'hudson.plugins.git.extensions.impl.PathRestriction' { excludedRegions('.*') }
                }
            }
            git {
                remote {
                    url(Constants.PIPELINES_REPO)
                    credentials(Constants.SSH_KEY_ID)
                }
                branch('*/master')
                extensions {
                    relativeTargetDirectory(Constants.DEFAULT_PIPELINES_DIR)
                    localBranch('master')
                }
                configure { node ->
                    node / 'extensions' / 'hudson.plugins.git.extensions.impl.PathRestriction' { excludedRegions('.*') }
                }
            }
        }
        dslJob.configure configureEnvInject(getRegeneratorEnvironmentPropertiesScript(dslFactory))
        dslJob.steps {
            groovyCommand(prepareEnvironmentToRegeneratePipeline(dslFactory)) {
                groovyInstallation(Constants.GROOVY_VERSION)
                def path = "job/${jenkinsJobName.replaceAll('/', '/job/')}"
                def url = "${Constants.JENKINS_URL}/${path}/lastSuccessfulBuild/api/json?tree=timestamp,id"
                scriptParam(url)
                scriptParam('$BUILD_CAUSE')
            }
            conditionalSteps {
                condition {
                    fileExists('regenerate.pipeline', BaseDir.WORKSPACE)
                }
                runner('Run')
                steps {
                    dsl {
                        external(Constants.PIPELINE_BUILD_SCRIPTS_PATH + 'BuildPipeline.groovy')
                        removeAction('DELETE')
                        additionalClasspath('scripts/lib/*.jar')
                    }
                    shell(bashCommand('pipeline-update-version-CD.sh', this.pipeline.name))
                    systemGroovyCommand(getPipelineNextBuildNumberScript(dslFactory))
                }
            }

        }
        dslJob.configure configureAutoApprovalScriptlet()
        dslJob.publishers {
            extendedEmail {
                def jenkinsJobUrl = "${Constants.JENKINS_URL}/job/${jenkinsJobName.replaceAll('/', '/job/')}"
                recipientList('${EMAIL_RECIPIENTS}')
                defaultSubject("Jenkins - ${jenkinsJobName} is broken")
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

        dslJob.publishers {
            downstreamParameterized {
                trigger("${nextJob}") {
                    condition('SUCCESS')
                    parameters {
                        predefinedProp('BUILD_GIT_COMMIT', '${GIT_COMMIT}')
                    }
                }
            }
        }

    }

    private String getPipelineNextBuildNumberScript(dslFactory) {
        def pipelineNextBuildNumberScriptParams = [
                "##PIPELINE_JENKINS_PATH##": this.pipeline.jenkinsPath
        ]
        return getScript(dslFactory, 'pipeline-next-build-number-CD.groovy', pipelineNextBuildNumberScriptParams)
    }

}
