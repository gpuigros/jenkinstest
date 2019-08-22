import DeleteOldJobs
import JobsFactory


def out= getBinding().out;
def deleteJobs=new DeleteOldJobs(out)
def jobsFactory=new JobsFactory(out)
def basePath="${PARENT_FOLDER}/${PARENT_FOLDER}"


deleteJobs.deleteOld("${PARENT_FOLDER}","${PARENT_FOLDER}_", "REGENERATOR")


println "basePath = ${basePath}"

jobsFactory.createJob(
        "${basePath}_job-dsl-test_BUILD_0",
        String "BUILD",
        String "git://github.com/gpuigros/jenkinstest.git")

def job1=job("${basePath}_job-dsl-test_BUILD") {
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