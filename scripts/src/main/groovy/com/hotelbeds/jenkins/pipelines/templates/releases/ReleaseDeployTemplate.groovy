package com.hotelbeds.jenkins.pipelines.templates.releases

import com.hotelbeds.jenkins.pipelines.templates.BaseDeployTemplate

/**
 * The Class AngularJSDeployTemplate.
 */
class ReleaseDeployTemplate extends BaseDeployTemplate {

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
            jobIdentifier: 34bc0e2f-8782-4311-bcf4-8c921d7fe9af
            jobOptions:
            - environment=${Environment.findDeploymentEnvironmentCode(template.profile, template.environment)}
            - service_name=${pipeline.name}
            - version=\\${BUILD_VERSION}
'''

    /* (non-Javadoc)
     * @see com.hotelbeds.jenkins.pipelines.templates.Template#getTemplate()
     */

    @Override
    public Object getTemplate() {
        return TEMPLATE
    }
}
