package com.hotelbeds.jenkins.pipelines.templates.releases

import com.hotelbeds.jenkins.pipelines.Environment
import com.hotelbeds.jenkins.pipelines.templates.Template


class ReleaseTemplate implements Template {

    def static final RELEASE_TEMPLATE = '''
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

        - name: INTEGRATION_TESTS
          type: ${JobType.MAVEN.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Integration tests
          jenkinsAgentTags: ${pipeline.tags}
          command: clean verify -Dtest=ignoreUnitTests -DfailIfNoTests=false -Dspring.profiles.active=dev,ci
          publishers:
            junit:
            - '**/target/failsafe-reports/*.xml'

        - name: CODE_ANALYSIS
          type: ${JobType.MAVEN.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Code analysis
          jenkinsAgentTags: ON_PREMISE
          command: sonar:sonar -Dsonar.analysis.mode=publish -Dsonar.buildbreaker.skip=false

        - name: PACKAGE
          type: ${JobType.MAVEN.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Create RPM
          jenkinsAgentTags: ${pipeline.tags}
          command: clean package -U -P deploy-rpm -Drpm.version=\\${RPM_VERSION} -Drpm.release=\\${RPM_RELEASE} -Dgit.commit.id=\\${BUILD_GIT_COMMIT} 
          sharedArtifacts:
            - UPLOAD_ARTIFACT_${pipeline.environment}_${pipeline.profiles.join("_REPO:\\n              - \'" + pipeline.name + "-rpm/target/rpm/" + pipeline.name + "/RPMS/noarch/" + pipeline.name + "-\\$" + pipeline.OPEN_BRACE + "RPM_VERSION" + pipeline.CLOSE_BRACE + "-\\$" + pipeline.OPEN_BRACE + "RPM_RELEASE" + pipeline.CLOSE_BRACE + ".noarch.rpm\'\\n            - UPLOAD_ARTIFACT_" + pipeline.environment + "_")}_REPO:\\n              - \'${pipeline.name}-rpm/target/rpm/${pipeline.name}/RPMS/noarch/${pipeline.name}-\\${RPM_VERSION}-\\${RPM_RELEASE}.noarch.rpm\'
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
            ReleaseDeployTemplate deployTemplate = new ReleaseDeployTemplate(parameters)
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
        return RELEASE_TEMPLATE
    }
}
