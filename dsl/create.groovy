import DeleteOldJobs

//def out= getBinding().out;
//def deleteJobs=new DeleteOldJobs(out)
//deleteJobs.deleteOld("${PARENT_FOLDER}")
println GroovySystem.version

job('DSL/job-dsl-test_BUILD') {
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
      maven {
            goals('-e clean test')
            mavenInstallation('maven-3.6.0')

        }

        
    }
}