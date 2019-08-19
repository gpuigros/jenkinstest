package com.hotelbeds.jenkins.pipelines.prbuilder

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.pipelines.Technology
import com.hotelbeds.jenkins.pipelines.jobs.Job
import com.hotelbeds.jenkins.pipelines.jobs.ScmRepo
import com.hotelbeds.jenkins.pipelines.stages.Stage


class PullRequestAnalysisJob extends PullRequestBuilderAbstractJob {

    static final String JOB_NAME = 'PR_BUILDER_ANALYSIS'

    PullRequestAnalysisJob(Pipeline pipeline) {
        super(pipeline)
        Map scmRepoConfig = new HashMap<String, String>()
        scmRepoConfig.put("url", pipeline.scmRepository)
        scmRepoConfig.put("branch", '${BUILD_GIT_COMMIT}')

        this.name = JOB_NAME
        this.showName = 'CODE ANALYSIS'
        this.scm = new ScmRepo(scmRepoConfig)

        /* Necessary to configure the same stage that the first PR builder job */
        pipeline.stages.each { Stage pipelineStage ->
            if (pipelineStage.name == PR_STAGE_NAME) {
                this.stage = pipelineStage
            }
        }

    }


    @Override
    def customBuild() {

        dslJob.wrappers {
            preScmSteps() {
                steps {
                    shell(configureGitScript)
                }
                failOnError(false)
            }
        }

        dslJob.steps {
            if (this.stage.pipeline.sharedArtifacts.containsKey(this.name)) {
                this.stage.pipeline.sharedArtifacts.get(this.name).each { fromJobName, artifacts ->
                    Job fromJob = this.stage.pipeline.findJobByName(fromJobName)
                    String artifactPrefixPath = Constants.DEFAULT_PROJECT_DIR + "/"
                    copyArtifacts(fromJob.getJenkinsJobName()) {
                        includePatterns(artifactPrefixPath + artifacts.toString().replaceAll("\\[|\\]", "").split(",")*.trim().join(", " + artifactPrefixPath))
                        buildSelector { latestSuccessful(true) }
                    }
                }
            }
        }

        setTechnologySteps()

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

    Closure setJavaSteps() {
        def analysisCommand = 'sonar:sonar -Dsonar.analysis.mode=preview -Dsonar.issuesReport.console.enable=true'
        return {
            maven {
                mavenInstallation('maven-3')
                goals(analysisCommand)
                rootPOM("${Constants.DEFAULT_PROJECT_DIR}/pom.xml")
            }
        }
    }


}
