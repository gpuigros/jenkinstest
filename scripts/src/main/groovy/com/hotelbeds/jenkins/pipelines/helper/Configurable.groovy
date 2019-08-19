package com.hotelbeds.jenkins.pipelines.helper

interface Configurable {

    //Implement a method that configure templates that will be added as jobs using the method addTemplate
    def configureTemplate(pipeline, stageConfig, jobProfiles, templateParameters)

}

