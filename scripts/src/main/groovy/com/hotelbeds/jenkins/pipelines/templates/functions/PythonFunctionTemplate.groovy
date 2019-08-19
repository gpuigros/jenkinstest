package com.hotelbeds.jenkins.pipelines.templates.functions

import com.hotelbeds.jenkins.pipelines.Environment
import com.hotelbeds.jenkins.pipelines.Profile
import com.hotelbeds.jenkins.pipelines.templates.Template


class PythonFunctionTemplate implements Template {

    def static final PYTHON_FUNCTION_TEMPLATE = '''
pipeline:
  name: ${pipeline.name}
  packageType: ${pipeline.packageType}
  profiles: \\n    - ${pipeline.profiles.join('\\n    - ')}   
  stages:

    - name: BUILD      
      jobs:

        - name: BUILD
          type: ${JobType.PYTHON.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Build
          jenkinsAgentTags: ${pipeline.tags}
          command: compile_sources

        - name: UNIT_TESTS
          type: ${JobType.PYTHON.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Unit tests          
          jenkinsAgentTags: ${pipeline.tags}
          command: clean run_unit_tests
          publishers:
            junit:
            - '**/target/reports/TEST*.xml'

        - name: CODE_ANALYSIS
          type: ${JobType.PYTHON.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Code analysis
          jenkinsAgentTags: ON_PREMISE
          command: analyze
          publishers:
            html:
              reportFile: index.html
              reportPath: target/reports/coverage_html
              reportTitle: Coverage Report

        - name: PACKAGE
          type: ${JobType.PYTHON.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Create ZIP
          jenkinsAgentTags: ${pipeline.tags}
          command: package_lambda_code -P release=\\${RPM_RELEASE} 
          sharedArtifacts:
            - UPLOAD_ARTIFACT_TEST_${pipeline.profiles.join("_REPO:\\n              - 'target/" + pipeline.name + "-\\$" + pipeline.OPEN_BRACE + "BUILD_VERSION" + pipeline.CLOSE_BRACE + ".zip'\\n            - UPLOAD_ARTIFACT_TEST_")}_REPO:\\n              - 'target/${pipeline.name}-\\${BUILD_VERSION}.zip\'
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
                def parameters = [
                        environment: environment, profile: profile, pipeline: pipeline
                ]
                PythonFunctionDeployTemplate deployTemplate = new PythonFunctionDeployTemplate(parameters)
                pipeline.render(deployTemplate)
            }.join("\n")
        }.join("\n")
    }

    /* (non-Javadoc)
     * @see com.hotelbeds.jenkins.pipelines.templates.Template#getTemplate()
     */

    @Override
    public Object getTemplate() {
        return PYTHON_FUNCTION_TEMPLATE
    }
}
