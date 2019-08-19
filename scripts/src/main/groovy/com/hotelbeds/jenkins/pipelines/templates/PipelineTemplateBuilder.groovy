package com.hotelbeds.jenkins.pipelines.templates

import com.hotelbeds.jenkins.Constants
import groovy.transform.builder.Builder


import com.hotelbeds.jenkins.pipelines.Environment
import com.hotelbeds.jenkins.pipelines.jobs.JobType

@Builder
public class PipelineTemplateBuilder implements Renderable {

    def final static OPEN_BRACE = '#@#'

    def final static CLOSE_BRACE = '#~#'

    def tags

    def template

    def name

    def profiles

    def workspace

    def scmRepository

    def solutionAlias

    def environment

    def packageType


    @Override
    def render() {
        validate()
        render(template)
    }

    /**
     * Render.
     *
     * #@# is the equivalent character of '{' and #~# is the equivalent character of '}'. These replacements are mandatory inside join closures
     *
     * @param template the template
     * @return the java.lang. object
     */
    def render(Template template) {
        def engine = new groovy.text.SimpleTemplateEngine()
        def output = engine.createTemplate(template.getTemplate()).make([pipeline: this, JobType: JobType, Environment: Environment, template: template]).toString()
        return output.replaceAll(OPEN_BRACE, '{').replaceAll(CLOSE_BRACE, '}')
    }

    /**
     * Render.
     *
     * @param templateClassName the template class name
     * @return the java.lang. object
     */
    def render(String templateClassName, parameters = [:]) {
        Template template = Class.forName(templateClassName).newInstance(parameters)
        render(template)
    }

    /**
     * Validate.
     *
     * @return the java.lang. object
     */
    def validate() {
        if (this.name == null) throw new Error("name can not be null")
    }

    /* (non-Javadoc)
     * @see com.hotelbeds.jenkins.pipelines.templates.Renderable#toFile()
     */

    @Override
    public File toFile() {
        File projectContainer = new File("${workspace}/${Constants.DEFAULT_PROJECT_DIR}")
        projectContainer.mkdirs()
        return toFile(projectContainer)
    }

    /* (non-Javadoc)
     * @see com.hotelbeds.jenkins.pipelines.templates.Renderable#toFile(java.io.File)
     */

    @Override
    public File toFile(File container) {
        File pipelineConfigFile = new File(container, "pipeline.yml")
        pipelineConfigFile.createNewFile()
        pipelineConfigFile.write(this.render())
        return pipelineConfigFile
    }


    public File toFile(File container, String environmentName) {
        File pipelineConfigFile = new File(container, "pipeline-" + environmentName + ".yml")
        pipelineConfigFile.createNewFile()
        pipelineConfigFile.write(this.render())
        return pipelineConfigFile
    }
}
