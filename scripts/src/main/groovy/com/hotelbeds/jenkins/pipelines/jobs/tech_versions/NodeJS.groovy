package com.hotelbeds.jenkins.pipelines.jobs.tech_versions

enum NodeJS {

    NodeJS_8_9_4('NodeJS 8.9.4'),
    NodeJS_10_4_0('NodeJS 10.4.0')

    def code

    private NodeJS(Object code) {
        this.code = code
    }

}
