package com.hotelbeds.jenkins.pipelines.jobs


class ScmRepo {

    def url

    def branch


    ScmRepo(Map config) {
        url = config.url
        branch = config.branch
    }

}
