job('DSL/job-dsl-test_BUILD') {
    scm {
        git {
            remote {
                name('remoteB')
                url('git://github.com/gpuigros/jenkinstest.git')
            }
            extensions {
                wipeOutWorkspace()
                localBranch master
            }
        }
    }
    
    steps {
        maven('-e clean package')
    }
}