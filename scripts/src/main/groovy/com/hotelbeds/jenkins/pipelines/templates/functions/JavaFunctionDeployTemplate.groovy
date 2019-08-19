package com.hotelbeds.jenkins.pipelines.templates.functions

import com.hotelbeds.jenkins.pipelines.templates.BaseDeployTemplate

/**
 * The Class JavaFunctionDeployTemplate.
 */
class JavaFunctionDeployTemplate extends BaseDeployTemplate {

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
            jobIdentifier: 9470bdf4-984a-4bd1-acef-db8200bea5d3
            jobOptions:
            - environment=${Environment.findDeploymentEnvironmentCode(template.profile, template.environment)}
            - service_name=${pipeline.name}
            - version=\\${BUILD_VERSION}
${template.miniSmokeTestsStep()}
${template.endToEndTestsStep()}
${template.performanceTestsStep()}
'''

    /* (non-Javadoc)
     * @see com.hotelbeds.jenkins.pipelines.templates.Template#getTemplate()
     */

    @Override
    public Object getTemplate() {
        return TEMPLATE
    }
}
