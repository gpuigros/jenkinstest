package com.hotelbeds.jenkins.pipelines.templates.angularjs

import com.hotelbeds.jenkins.pipelines.templates.BaseDeployTemplate

/**
 * The Class AngularJSDeployTemplate.
 */
class AngularJSDeployTemplate extends BaseDeployTemplate {

/** The Constant TEMPLATE. */
    def static final TEMPLATE = '''
    - name: ${template.environment}_${template.profile.toUpperCase()}
      jobs:
${template.rdmApprovalStep()}
${template.uploadArtifactStep()}
        - name: DEPLOY_APPLICATION_${template.environment.toUpperCase()}_${template.profile.toUpperCase()}
          type: ${JobType.RUNDECK.code()}
          profiles: ${template.profile.toUpperCase()}
          showName: Deploy application
          jenkinsAgentTags: ${template.profile.toUpperCase()}
          command:
            jobIdentifier: 68f21d7c-02ac-465f-83d5-7d197dbc2f7c
            jobOptions:
            - environment=${Environment.findDeploymentEnvironmentCode(template.profile, template.environment)}
            - service_name=${pipeline.name}
            - bucket_name=${pipeline.name}-bucket
            - version=\\${BUILD_VERSION}
              
${template.frontEndToEndSmokeTestsStep()}
${template.frontEndToEndRegressionTestsStep()}
'''

    /* (non-Javadoc)
     * @see com.hotelbeds.jenkins.pipelines.templates.Template#getTemplate()
     */

    @Override
    public Object getTemplate() {
        return TEMPLATE
    }
}
