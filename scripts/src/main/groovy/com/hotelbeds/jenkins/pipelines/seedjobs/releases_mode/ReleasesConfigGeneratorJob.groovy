package com.hotelbeds.jenkins.pipelines.seedjobs.releases_mode

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Buildable
import com.hotelbeds.jenkins.pipelines.Profile
import com.hotelbeds.jenkins.pipelines.Validable

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.*

class ReleasesConfigGeneratorJob implements Buildable, Validable {

    String destinationPath


    ReleasesConfigGeneratorJob(destinationPath) {
        this.destinationPath = destinationPath
    }


    @Override
    def build(dslFactory) {
        def pipelineBuildScriptsPathParam = [
                "##PIPELINE_BUILD_SCRIPTS_PATH##": Constants.PIPELINE_BUILD_SCRIPTS_PATH
        ]
        def packagePipelineScripts = getScript(dslFactory, 'package-pipelines-scripts.sh', pipelineBuildScriptsPathParam)
        def pushYmlToRepository = getScript(dslFactory, 'git-push-yml-to-repo-RELEASES.sh')
        def pushYmlMetadata = getScript(dslFactory, 'git-push-yml-metadata.sh')
        def jenkinsListFolders = getScript(dslFactory, 'jenkins-list-folders.groovy')
        def nexusListRepos = getScript(dslFactory, 'nexus-list-repos.groovy')
        def configGeneratorEnvInject = getScript(dslFactory, 'envinject-config-generator.groovy')

        dslFactory.folder(destinationPath) {
            displayName(destinationPath.split('/')?.last()?.toUpperCase())
        }

        def jobName = "${this.destinationPath}/buildblock-pipeline_CONFIG_GENERATOR"

        if (!isValid()) {
            throw new Error("$jobName has validation errors")
        }

        dslFactory.job(jobName) {
            logRotator(-1, 10, -1, -1)
            parameters {
                stringParam('GIT_REPO_URL', 'ssh://git@stash.hotelbeds:7999/envtools/hello-world-releases.git', '')
                extensibleChoiceParameterDefinition {
                    name('SOLUTION_ALIAS')
                    choiceListProvider {
                        systemGroovyChoiceListProvider {
                            groovyScript {
                                script(nexusListRepos)
                                sandbox(false)
                                description("")
                                usePredefinedVariables(false)
                            }
                            defaultChoice('Top')
                        }
                    }
                    editable(false)
                }
                choiceParam('TECHNOLOGY', ['Spring Boot', 'AngularJS', 'Java Function', 'Python Function', 'ACE', 'Python'], '')
                choiceParam('RELEASEMENT_STRATEGY', ['CONTINUOUS_DEPLOYMENT', 'RELEASES'], '')
                stringParam('GIT_BRANCH', 'master', '[CONTINUOUS DEPLOYMENT]')
                stringParam('DEV_BRANCH', 'develop', '[RELEASES]')
                stringParam('TEST_BRANCH', 'release/LU2017-09', '[RELEASES] Put the current release branch')
                stringParam('STAGE_BRANCH', 'master', '[RELEASES]')
                extensibleChoiceParameterDefinition {
                    name('FOLDER')
                    choiceListProvider {
                        systemGroovyChoiceListProvider {
                            groovyScript {
                                script(jenkinsListFolders)
                                sandbox(false)
                                description("")
                                usePredefinedVariables(false)
                            }
                            defaultChoice('Top')
                        }
                    }
                    editable(false)
                }
            }
            label('master')
            multiscm {
                git {
                    remote {
                        url(Constants.PIPELINES_SCRIPTS_REPO)
                        credentials(Constants.ATLASDEPLOY_ID)
                    }
                    branch(Constants.PIPELINES_SCRIPTS_BRANCH)
                    extensions { relativeTargetDirectory(Constants.DEFAULT_SCRIPTS_DIR) }
                }
                git {
                    remote {
                        url(Constants.PIPELINES_REPO)
                        credentials(Constants.ATLASDEPLOY_ID)
                    }
                    branch('*/master')
                    extensions {
                        relativeTargetDirectory(Constants.DEFAULT_PIPELINES_DIR)
                    }
                }
                git {
                    remote {
                        url('${GIT_REPO_URL}')
                        credentials(Constants.ATLASDEPLOY_ID)
                    }
                    branch('*/master')
                    extensions { gitRepo -> relativeTargetDirectory(Constants.DEFAULT_PROJECT_DIR) }
                }
            }
            wrappers {
                preBuildCleanup()
            }

            configure configureExtendedChoiceParameter("PROFILES", Profile.values().size(), "PT_CHECKBOX", Profile.values().join(","), Profile.AWS.toString(), "||")
            configure configureEnvInject(configGeneratorEnvInject)
            steps {
                shell(packagePipelineScripts)
                systemGroovyScriptFile('${WORKSPACE}/' + Constants.PIPELINE_BUILD_SCRIPTS_PATH + 'com/hotelbeds/jenkins/launchers/ConfigurePipelineLauncher.groovy')
                shell(pushYmlToRepository)
                shell(pushYmlMetadata)
            }
        }
    }


    @Override
    boolean isValid() {
        return true
    }
}
