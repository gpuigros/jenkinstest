package com.hotelbeds.jenkins.pipelines.templates.metadata


import com.hotelbeds.jenkins.Constants;
import com.hotelbeds.jenkins.pipelines.templates.Renderable

import groovy.transform.builder.Builder


@Builder
class MetadataBuilder implements Renderable {

    def jenkinsPath

    def pipelineName

    def scmRepository

    def scmBranch

    def technology

    def template

    def workspace

    def releasementStrategy

    /* (non-Javadoc)
     * @see com.hotelbeds.jenkins.pipelines.templates.Renderable#render()
     */

    @Override
    public Object render() {
        validate()
        normalize()
        def engine = new groovy.text.SimpleTemplateEngine()
        return engine.createTemplate(template.getTemplate()).make([metadata: this, Constants: Constants]).toString()
    }

    /**
     * Normalize.
     *
     * @return the java.lang. object
     */
    def normalize() {
        this.jenkinsPath = this.jenkinsPath.toString().toLowerCase().replaceAll(' ', '-')
    }

    /* (non-Javadoc)
     * @see com.hotelbeds.jenkins.pipelines.templates.Renderable#toFile()
     */

    @Override
    public File toFile() {
        File pipelinesContainer = new File("${workspace}/${Constants.DEFAULT_PIPELINES_DIR}")
        pipelinesContainer.mkdirs();
        return toFile(pipelinesContainer)
    }

    /**
     * Validate.
     *
     * @return the java.lang. object
     */
    def validate() {
        if (this.scmRepository == null) throw new Error("scmRepository can not be null")
    }

    /* (non-Javadoc)
     * @see com.hotelbeds.jenkins.pipelines.templates.Renderable#toFile(java.io.File)
     */

    @Override
    public File toFile(File container) {
        String pipelineName = scmBranch == 'master' ? "${this.pipelineName}.yml" : "${this.pipelineName}${Constants.BRANCH_SEPARATOR}${this.scmBranch.replaceAll('/', Constants.BRANCH_DIRECTORY_SEPARATOR)}.yml"
        File pipelineMetadataFile = new File(container, pipelineName)
        pipelineMetadataFile.createNewFile()
        pipelineMetadataFile.write(this.render())
        return pipelineMetadataFile
    }
}
