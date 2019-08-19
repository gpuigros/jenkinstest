package com.hotelbeds.jenkins.pipelines.templates.python

import com.hotelbeds.jenkins.pipelines.templates.BaseDeployTemplate

/**
 * The Class PythonDeployTemplate.
 */
class PythonDeployTemplate extends BaseDeployTemplate {

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
            jobIdentifier: PUT-HERE-YOUR-RUNDECK-JOB-ID
            jobOptions:
            - rpm_version=\\${BUILD_VERSION} 
            - environment=${Environment.findDeploymentEnvironmentCode(template.profile, template.environment)}
            - service_name=${pipeline.name}
                     
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
