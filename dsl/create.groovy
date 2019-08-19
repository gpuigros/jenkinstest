job('DSL/job-dsl-test') {
    scm {
        git('git://github.com/gpuigros/jenkinstest.git')
        credentials('gpuigros-github')
    }
    
    steps {
        maven('-e clean pacakage')
    }
}