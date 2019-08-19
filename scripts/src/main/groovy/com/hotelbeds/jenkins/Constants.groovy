package com.hotelbeds.jenkins

class Constants {

    static def final GROOVY_VERSION = 'Groovy 2.4.5'

    static def final JENKINS_URL = 'http://jenkins.service'

    static def final PIPELINES_REPO = 'ssh://git@stash.hotelbeds:7999/envtools/jenkins-pipelines.git'

    static def final PIPELINES_SCRIPTS_BRANCH = '*/master'

    static def final PIPELINES_SCRIPTS_REPO = 'ssh://git@stash.hotelbeds:7999/envtools/jenkins-pipelines-scripts.git'

    static def final SSH_KEY_ID = 'stash-ssh-key'

    static def final ATLASDEPLOY_ID = 'd2f88d66-eec4-4cf4-8eb1-9b556dc82324'

    static def final STASH_QA_PROJECT = 'ssh://git@stash.hotelbeds:7999/qa'

    static final String DEFAULT_ARTIFACT_GROUP_ID = 'com.hotelbeds'

    static final String BRANCH_DIRECTORY_SEPARATOR = '#'

    static final String BRANCH_PLACEHOLDER = '$'

    static final String BRANCH_SEPARATOR = '@'

    static final String CONTINUOUS_DEPLOYMENT_RELEASEMENT_STRATEGY = 'CONTINUOUS_DEPLOYMENT'

    static final String RELEASES_RELEASEMENT_STRATEGY = 'RELEASES'

    static final String APPROVE_PREFFIX_PARAM_VALUE = '//@Autoapproved'

    static final AUTOAPPROVED_GROOVY_SCRIPTS_BUILDER_ID_GRABBED_FROM_XML = '1502891139198_15'

    static final String SCRIPTS_PATH = 'scripts/src/main/groovy/com/hotelbeds/jenkins/scripts/'

    static final String PIPELINE_BUILD_SCRIPTS_PATH = 'scripts/src/main/groovy/'

    static final String DEFAULT_PROJECT_DIR = 'project'

    static final String DEFAULT_SCRIPTS_DIR = 'scripts'

    static final String DEFAULT_PIPELINES_DIR = 'pipelines'

    static final String ALLURE_DEFAULT_PATH = 'target/allure-results'

    static final String ALLURE_DEFAULT_VERSION = 'allure1'

    static final String ECR_URL = '339970725716.dkr.ecr.eu-west-1.amazonaws.com'

    static final String ECR_REGION = 'eu-west-1'


}
