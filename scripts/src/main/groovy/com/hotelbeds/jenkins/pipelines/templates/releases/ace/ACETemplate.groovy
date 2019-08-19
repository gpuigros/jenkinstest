package com.hotelbeds.jenkins.pipelines.templates.releases.ace

import com.hotelbeds.jenkins.pipelines.templates.Template


class ACETemplate implements Template {

    def static final ACE_TEMPLATE = '''
pipeline:
  name: ${pipeline.name}
  packageType: ${pipeline.packageType}
  profiles: \\n    - ${pipeline.profiles.join('\\n    - ')}
  stages:

    - name: BUILD      
      jobs:

        - name: BUILD
          type: ${JobType.ACE.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Build
          jenkinsAgentTags: acedaemon
          command: build

        - name: PACKAGE
          type: ${JobType.ACE.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Create RPM
          jenkinsAgentTags: acedaemon 
          command: package
          sharedArtifacts:
            - UPLOAD_ARTIFACT_${pipeline.environment}_${pipeline.profiles.join("_REPO:\\n              - '" + pipeline.name + "-\\$" + pipeline.OPEN_BRACE + "BUILD_VERSION" + pipeline.CLOSE_BRACE + ".x86_64.rpm'\\n            - UPLOAD_ARTIFACT_" + pipeline.environment + "_")}_REPO:\\n              - '${pipeline.name}-\\${BUILD_VERSION}.x86_64.rpm\'
              
${template.renderSubtemplates(pipeline)}
'''


    def profiles

    def environment

    /**
     * Render subtemplates.
     *
     * @param pipeline the pipeline
     * @param environment the environment
     * @return the java.lang. object
     */
    def renderSubtemplates(pipeline) {
        profiles.collect { profile ->
            def parameters = [
                    environment: environment, profile: profile, pipeline: pipeline
            ]
            ACEDeployTemplate deployTemplate = new ACEDeployTemplate(parameters)
            try {
                pipeline.render(deployTemplate)
            } catch (IllegalArgumentException e) {
                println "${profile}_${environment} does not exist."
                return ''
            }
        }.join("\n")
    }

    /* (non-Javadoc)
     * @see com.hotelbeds.jenkins.pipelines.templates.Template#getTemplate()
     */

    @Override
    public Object getTemplate() {
        return ACE_TEMPLATE
    }
}
