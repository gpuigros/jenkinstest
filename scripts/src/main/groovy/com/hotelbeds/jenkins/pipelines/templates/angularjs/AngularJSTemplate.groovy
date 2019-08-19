package com.hotelbeds.jenkins.pipelines.templates.angularjs

import com.hotelbeds.jenkins.pipelines.templates.Template


class AngularJSTemplate implements Template {

    def static final ANGULARJS_TEMPLATE = '''
pipeline:
  name: ${pipeline.name}
  packageType: ${pipeline.packageType}
  profiles: \\n    - ${pipeline.profiles.join('\\n    - ')}   
  stages:

    - name: BUILD      
      jobs:

        - name: BUILD
          type: ${JobType.NPM.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Build
          jenkinsAgentTags: ${pipeline.tags}
          command: run build

        - name: UNIT_TESTS
          type: ${JobType.NPM.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Unit tests
          jenkinsAgentTags: ${pipeline.tags}
          command: unit_tests

        - name: CODE_ANALYSIS
          type: ${JobType.NPM.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Code analysis
          jenkinsAgentTags: ON_PREMISE
          command: code_analysis

        - name: PACKAGE
          type: ${JobType.NPM.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Create RPM
          jenkinsAgentTags: ${pipeline.tags}
          command: run package --version \\${RPM_VERSION} --release \\${RPM_RELEASE}
          sharedArtifacts:
            - UPLOAD_ARTIFACT_TEST_${pipeline.profiles.join("_REPO:\\n              - \'brass_build/RPMS/noarch/" + pipeline.name + "-\\$" + pipeline.OPEN_BRACE + "RPM_VERSION" + pipeline.CLOSE_BRACE + "-\\$" + pipeline.OPEN_BRACE + "RPM_RELEASE" + pipeline.CLOSE_BRACE + ".noarch.rpm\'\\n            - UPLOAD_ARTIFACT_TEST_")}_REPO:\\n              - \'brass_build/RPMS/noarch/${pipeline.name}-\\${RPM_VERSION}-\\${RPM_RELEASE}.noarch.rpm\'

${template.renderSubtemplates(pipeline)}
'''

    def profiles

    /**
     * Render subtemplates.
     *
     * @param pipeline the pipeline
     * @return the java.lang. object
     */
    def renderSubtemplates(pipeline) {
        profiles.collect { profile ->
            def environments = ['TEST', 'STAGE', 'LIVE', 'INT']
            environments.collect { environment ->
                AngularJSDeployTemplate deployTemplate = new AngularJSDeployTemplate([environment: environment, profile: profile, pipeline: pipeline])
                pipeline.render(deployTemplate)
            }.join("\n")
        }.join("\n")
    }

    /* (non-Javadoc)
     * @see com.hotelbeds.jenkins.pipelines.templates.Template#getTemplate()
     */

    @Override
    public Object getTemplate() {
        return ANGULARJS_TEMPLATE
    }
}
