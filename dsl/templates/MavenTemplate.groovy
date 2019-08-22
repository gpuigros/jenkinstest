package templates

class MavenTemplate {
    static void create(job, config) {
        job.with {
            description("Builds all pull requests opened against <code>${config.repo}</code>.<br><br><b>Note</b>: This job is managed <a href='git://github.com/gpuigros/jenkinstest.git'>programmatically</a>; any changes will be lost.")
            deliveryPipelineConfiguration(config.stageName)
            scm {
                git {
                    remote {
                        name('remoteB')
                        url(config.repo)
                    }
                    extensions {
                        wipeOutWorkspace()
                    }
                }
            }
            
            steps {
                maven {
                    goals(config.command)
                    mavenInstallation('maven-3.6.0')

                }
            }
            
        }

        if (config.upstreamJob!=null){
            job.with {
                triggers {
                    upstream(config.upstreamJob, "SUCCESS")
                }
            }
        }

    }
}