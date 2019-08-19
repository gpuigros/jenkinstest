package com.hotelbeds.jenkins.pipelines.jobs.tech_versions

enum Python {

    PYTHON2('System-CPython-2.7', 'python2.7'),
    PYTHON36('System-CPython-3.6.8', 'python3.6')

    def name
    def code

    private Python(Object name, Object code) {
        this.name = name
        this.code = code
    }

}
