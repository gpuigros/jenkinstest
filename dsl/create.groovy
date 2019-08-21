import DeleteOldJobs



def out= getBinding().out;
def deleteJobs=new DeleteOldJobs(out)
def basePath="${PARENT_FOLDER}/${PARENT_FOLDER}"


deleteJobs.deleteOld("${PARENT_FOLDER}","${PARENT_FOLDER}_", "REGENERATOR")


println "basePath = ${basePath}"


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