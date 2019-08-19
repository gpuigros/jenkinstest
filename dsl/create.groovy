job('DSL/job-dsl-test_BUILD') {
    scm {
        git('git://github.com/gpuigros/jenkinstest.git')
        //credentials('gpuigros-github')
        extensions {
                wipeOutWorkspace()
                localBranch master
            }
    }
    
    steps {
        maven('-e clean package')
    }
}