package com.hotelbeds.jenkins.pipelines.templates.releases.ace

import com.hotelbeds.jenkins.pipelines.templates.BaseDeployTemplate

/**
 * The Class AceDeployTemplate.
 */
class ACEDeployTemplate extends BaseDeployTemplate {

/** The Constant TEMPLATE. */
    def static final TEMPLATE = '''
    - name: ${template.environment}_${template.profile.toUpperCase()}
      jobs:
${template.uploadArtifactStep()}
        
        - name: RDM_APPROVAL_${template.environment.toUpperCase()}_${template.profile.toUpperCase()}
          type: rdm_approval
          profiles: ${template.profile.toUpperCase()}
          showName: RDM approval
          jenkinsAgentTags: acedaemon
        
        - name: DEPLOY_APPLICATION_DAEMON_${template.environment.toUpperCase()}_${template.profile.toUpperCase()}
          type: ${JobType.RUNDECK.code()}
          profiles: ${template.profile.toUpperCase()}
          showName: Deploy application DAEMON
          jenkinsAgentTags: acedaemon
          command:
            jobIdentifier: 6bb94a58-1094-47b4-9f54-d9dfd8159445
            jobOptions:
            - environment=${Environment.findDeploymentEnvironmentCode(template.profile, template.environment)}
            - service_name=${pipeline.name}
            - version=\\${BUILD_VERSION}
              
        - name: DEPLOY_APPLICATION_DBWRITER_${template.environment.toUpperCase()}_${template.profile.toUpperCase()}
          type: ${JobType.RUNDECK.code()}
          profiles: ${template.profile.toUpperCase()}
          showName: Deploy application DBWRITER
          jenkinsAgentTags: acedaemon
          command:
            jobIdentifier: d266813b-fdab-445f-a6d2-01eab5fb337e
            jobOptions:
            - environment=${Environment.findDeploymentEnvironmentCode(template.profile, template.environment)}
            - service_name=ace-hotel-writer
            - version=\\${BUILD_VERSION}
            - repo_ubication=shared${template.profile.toLowerCase()}
            - solution=hbgplat_distribution_hotel
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
