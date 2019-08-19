package com.hotelbeds.jenkins.pipelines.templates.python

import com.hotelbeds.jenkins.pipelines.Environment
import com.hotelbeds.jenkins.pipelines.Profile
import com.hotelbeds.jenkins.pipelines.templates.Template

class PythonTemplate implements Template {

    def static final PYTHON_TEMPLATE = '''
pipeline:
  name: ${pipeline.name}
  pythonVersion: python3.6
  profiles: \\n    - ${pipeline.profiles.join('\\n    - ')}   
  stages:

    - name: BUILD      
      jobs:

        - name: BUILD
          type: ${JobType.PYTHON.code()}
          profiles: \\n            - ${pipeline.profiles.join('\\n            - ')}
          showName: Create RPM
          jenkinsAgentTags: ${pipeline.tags}
          command: setup.py --command-packages=setuptools_hbg.command bdist_rpm_hbg --release=\\${RPM_RELEASE} --no-autoreq --binary-only --python=/usr/bin/python3.6
          sharedArtifacts:
            - UPLOAD_ARTIFACT_TEST_${pipeline.profiles.join("_REPO:\\n              - '" + "dist/" + pipeline.name + "-\\$" + pipeline.OPEN_BRACE + "RPM_VERSION" + pipeline.CLOSE_BRACE + "-\\$" + pipeline.OPEN_BRACE + "RPM_RELEASE" + pipeline.CLOSE_BRACE + ".noarch.rpm'\\n            - UPLOAD_ARTIFACT_TEST_")}_REPO:\\n              - 'dist/${pipeline.name}-\\${RPM_VERSION}-\\${RPM_RELEASE}.noarch.rpm\'
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
                PythonDeployTemplate deployTemplate = new PythonDeployTemplate(parameters)
                pipeline.render(deployTemplate)
            }.join("\n")
        }.join("\n")
    }


    /* (non-Javadoc)
     * @see com.hotelbeds.jenkins.pipelines.templates.Template#getTemplate()
     */

    @Override
    public Object getTemplate() {
        return PYTHON_TEMPLATE
    }
}
