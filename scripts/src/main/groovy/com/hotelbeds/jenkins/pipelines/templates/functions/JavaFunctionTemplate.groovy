package com.hotelbeds.jenkins.pipelines.templates.functions

import com.hotelbeds.jenkins.pipelines.Environment
import com.hotelbeds.jenkins.pipelines.Profile
import com.hotelbeds.jenkins.pipelines.templates.Template


class JavaFunctionTemplate implements Template {

    def static final JAVA_FUNCTION_TEMPLATE = '''
pipeline:
  name: ${pipeline.name}
  packageType: ${pipeline.packageType}
  profiles: \\n    - ${pipeline.profiles.join('\\n    - ')}   
  stages:

    - name: BUILD      
      jobs:

        - name: BUILD
          type: ${JobType.MAVEN.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Build
          jenkinsAgentTags: ${pipeline.tags}
          command: clean compile -X -e -U

        - name: UNIT_TESTS
          type: ${JobType.MAVEN.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Unit tests          
          jenkinsAgentTags: ${pipeline.tags}
          command: clean test -X -e
          sharedArtifacts:
            - CODE_ANALYSIS:
              - '**/target/coverage-reports/jacoco-ut.exec'
              - '**/target/surefire-reports/*.xml'
              - '**/target/classes/**/*.class'
          publishers:
            junit:
            - '**/target/surefire-reports/*.xml'

        - name: CODE_ANALYSIS
          type: ${JobType.MAVEN.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Code analysis
          jenkinsAgentTags: ON_PREMISE
          command: sonar:sonar -Dsonar.analysis.mode=publish -Dsonar.buildbreaker.skip=false

        - name: PACKAGE
          type: ${JobType.MAVEN.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Create ZIP
          jenkinsAgentTags: ${pipeline.tags}
          command: clean package -U -Dbuild.version=\\${BUILD_VERSION} -Dgit.commit.id=\\${BUILD_GIT_COMMIT} 
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
                JavaFunctionDeployTemplate deployTemplate = new JavaFunctionDeployTemplate(parameters)
                pipeline.render(deployTemplate)
            }.join("\n")
        }.join("\n")
    }

    /* (non-Javadoc)
     * @see com.hotelbeds.jenkins.pipelines.templates.Template#getTemplate()
     */

    @Override
    public Object getTemplate() {
        return JAVA_FUNCTION_TEMPLATE
    }
}
