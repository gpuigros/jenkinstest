package com.hotelbeds.jenkins.pipelines.jobs


enum JobType {

    TEMPLATE,
    FREE,
    MAVEN,
    NEXUS,
    NPM,
    RDM_APPROVAL,
    RUNDECK,
    ACE,
    PYTHON,
    DOCKER


    String code() {
        return this.name().toLowerCase()
    }

}
