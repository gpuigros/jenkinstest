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
        maven('-e clean package',null, null, mavenInstallation('maven-3.6.0'))
        
    }
}