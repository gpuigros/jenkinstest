@Grab('org.yaml:snakeyaml:1.17')
import DeleteOldJobs
import JobsFactory
import templates.*

def out= getBinding().out;
def deleteJobs=new DeleteOldJobs(out)
def jobsFactory=new JobsFactory(out,this)
def basePath="${PARENT_FOLDER}/${PARENT_FOLDER}"


deleteJobs.deleteOld("${PARENT_FOLDER}","${PARENT_FOLDER}_", "REGENERATOR")


println "basePath = ${basePath}"
//def job11=job("${basePath}_job-dsl-test_BUILD_0")
//jobsFactory.createJob(job11)

/*
jobsFactory.createJob(
        "${basePath}_job-dsl-test_BUILD_0",
        "BUILD",
        "git://github.com/gpuigros/jenkinstest.git")
*/
String config="name: jenkinstest \n" + "repo: git://github.com/gpuigros/jenkinstest.git \n"  + "command: -e clean package\n"  + "downstreamJob: ${basePath}_job-dsl-test_TEST\n"
//def pipelineMetadata = yaml.load(new FileReader(System.getenv("WORKSPACE")+"/pipeline.yml)
def pipelineMetadata = yaml.load(config)

MavenTemplate.create(job("${basePath}_job-dsl-test_BUILD_0"), pipelineMetadata)


def job1=job("${basePath}_job-dsl-test_BUILD") {
    description("Builds all pull requests opened against <code>${config.repo}</code>.<br><br><b>Note</b>: This job is managed <a href='git://github.com/gpuigros/jenkinstest.git'>programmatically</a>; any changes will be lost.")
    deliveryPipelineConfiguration('BUILD')
    scm {
        git {
            remote {
                name('remoteB')
                url('git://github.com/gpuigros/jenkinstest.git')
            }
            extensions {
                wipeOutWorkspace()
            }
        }
    }
    
    steps {
        maven {
            goals('-e clean package')
            mavenInstallation('maven-3.6.0')

        }
    }
    publishers {
        downstream("${basePath}_job-dsl-test_TEST")
        }
}

def job2=job("${basePath}_job-dsl-test_TEST") {
    deliveryPipelineConfiguration('BUILD')
    scm {
        git {
            remote {
                name('remoteB')
                url('git://github.com/gpuigros/jenkinstest.git')
            }
            extensions {
                wipeOutWorkspace()
            }
        }
    }
    
    steps {
        maven {
            goals('-e clean test')
            mavenInstallation('maven-3.6.0')

        }
       
    }
        publishers {
            downstream("${basePath}_job-dsl-test_DEPLOY")
            downstream("${basePath}_job-dsl-test_DEPLOY2")
        }
}

def job3=job("${basePath}_job-dsl-test_DEPLOY") {
    deliveryPipelineConfiguration('DEPLOY')
    scm {
        git {
            remote {
                name('remoteB')
                url('git://github.com/gpuigros/jenkinstest.git')
            }
            extensions {
                wipeOutWorkspace()
            }
        }
    }
    
    steps {
        maven {
            goals('-e clean package')
            mavenInstallation('maven-3.6.0')

        }

    }
}
def job4=job("${basePath}_job-dsl-test_DEPLOY2") {
    deliveryPipelineConfiguration('DEPLOY2')
    scm {
        git {
            remote {
                name('remoteB')
                url('git://github.com/gpuigros/jenkinstest.git')
            }
            extensions {
                wipeOutWorkspace()
            }
        }
    }
    
    steps {
        maven {
            goals('-e clean package')
            mavenInstallation('maven-3.6.0')

        }

    }
}