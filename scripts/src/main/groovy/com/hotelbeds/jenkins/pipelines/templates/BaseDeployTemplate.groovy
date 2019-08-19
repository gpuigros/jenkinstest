package com.hotelbeds.jenkins.pipelines.templates

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Environment
import com.hotelbeds.jenkins.pipelines.Profile
import com.hotelbeds.jenkins.pipelines.Provider
import com.hotelbeds.jenkins.pipelines.jobs.JobType


abstract class BaseDeployTemplate extends BaseTemplate {

    def profile

    def environment

    def pipeline

    String getJobNameSuffix() {
        return environment.toUpperCase() + '_' + profile.toUpperCase()
    }

    def rdmApprovalStep = {
        if (Environment.isLive(environment) || Environment.isInt(environment)) {
            return '''        - name: RDM_APPROVAL_''' + getJobNameSuffix() + '''
          type: ''' + JobType.RDM_APPROVAL.code() + '''
          profiles: ''' + profile.toUpperCase() + '''
          showName: RDM approval
          jenkinsAgentTags: ''' + profile.toUpperCase()
        } else {
            return GString.EMPTY
        }
    }

    def endToEndTestsStep = {
        if (Environment.isTest(environment)) {
            return '''        - name: END_TO_END_TESTS_''' + getJobNameSuffix() + '''
          scm:
            url: ''' + Constants.STASH_QA_PROJECT + '''/''' + pipeline.name + '''-qa.git
            branch: master          
          type: ''' + JobType.MAVEN.code() + '''
          profiles: ''' + profile.toUpperCase() + '''
          showName: Automated acceptance test
          jenkinsAgentTags: ''' + profile.toUpperCase() + '''
          command: test -P ''' + environment.toLowerCase() + '''
          publishers:
            allure:
              path: ''' + Constants.ALLURE_DEFAULT_PATH + '''
              version: ''' + Constants.ALLURE_DEFAULT_VERSION
        } else {
            return GString.EMPTY
        }
    }

    def frontEndToEndSmokeTestsStep = {
        return '''        - name: E2E_SMOKE_TEST_''' + getJobNameSuffix() + '''   
          type: ''' + JobType.NPM.code() + '''
          profiles: ''' + profile.toUpperCase() + '''
          showName: End to end smoke tests
          jenkinsAgentTags: ''' + profile.toUpperCase() + '''
          command: e2e_smoke_test --params.host=[HOST_URL] --seleniumAddress=http://10.162.2.130:4444/wd/hub
          '''
    }

    def frontEndToEndRegressionTestsStep = {
        return '''        - name: E2E_REGRESSION_TEST_''' + getJobNameSuffix() + '''        
          type: ''' + JobType.NPM.code() + '''
          profiles: ''' + profile.toUpperCase() + '''
          showName: End to end regression tests
          jenkinsAgentTags: ''' + profile.toUpperCase() + '''
          command: e2e_regression_test --params.host=[HOST_URL] --seleniumAddress=http://10.162.2.130:4444/wd/hub
          '''
    }

    def miniSmokeTestsStep = {
        if (Environment.isDev(environment)) {
            return '''        - name: MINI_SMOKE_TESTS_''' + getJobNameSuffix() + '''
          scm:
            url: ''' + Constants.STASH_QA_PROJECT + '''/''' + pipeline.name + '''-qa.git
            branch: master
          type: ''' + JobType.MAVEN.code() + '''
          profiles: ''' + profile.toUpperCase() + '''
          showName: Mini smoke tests
          jenkinsAgentTags: ''' + profile.toUpperCase() + '''
          command: test -Dgroup=mini-smoke -P ''' + environment.toLowerCase() + '''
          publishers:
            allure:
              path: ''' + Constants.ALLURE_DEFAULT_PATH + '''
              version: ''' + Constants.ALLURE_DEFAULT_VERSION
        } else {
            return GString.EMPTY
        }
    }


    def performanceTestsStep = {
        if (Environment.isStage(environment)) {
            return '''        - name: PERFORMANCE_TESTS_''' + getJobNameSuffix() + '''
          scm:
            url: ''' + Constants.STASH_QA_PROJECT + '''/''' + pipeline.name + '''-qa.git
            branch: master          
          type: ''' + JobType.MAVEN.code() + '''
          command: test -Dgroup=performance-tests -P ''' + environment.toLowerCase() + '''
          profiles: ''' + profile.toUpperCase() + '''
          showName: Performance tests
          jenkinsAgentTags: ''' + profile.toUpperCase()
        } else {
            return GString.EMPTY
        }
    }

    def uploadArtifactStep = {
        return '''        - name: UPLOAD_ARTIFACT_''' + getJobNameSuffix() + '''_REPO
          type: ''' + JobType.NEXUS.code() + '''
          profiles: ''' + profile.toUpperCase() + '''
          showName: Upload ''' + pipeline.packageType.toUpperCase() + ''' to ''' + environment.toUpperCase() + ''' Nexus
          nexusRepositoryId: ''' + findNexusRepositoryId(profile, environment, pipeline.solutionAlias) + '''
          environment: ''' + Environment.findSharedEnvironment(profile, environment) + '''
          jenkinsAgentTags: ''' + profile.toUpperCase()
    }

    /**
     * Find Nexus Repository Id.
     *
     * @param profile the profile
     * @param environmentName the environment name
     * @param solutionAlias the solution alias
     * @param packageType the package type
     * @return the nexus repository id
     */
    String findNexusRepositoryId(String profile, String environmentName, String solutionAlias) {
        Environment _environment = Environment.valueOf(environmentName)
        Profile _profile = Profile.valueOf(profile)
        String providerId = _profile.getProvider().code
        String businessId = _profile.getBusiness().code
        String regionId = _profile.getRegion().code
        String environmentId = _environment.code

        return environmentId + "-" + businessId + "-" + providerId + "-" + regionId + "-" + solutionAlias + "-repo"
    }


}
