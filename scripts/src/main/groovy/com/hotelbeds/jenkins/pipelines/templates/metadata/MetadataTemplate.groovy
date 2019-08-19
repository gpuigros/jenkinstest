package com.hotelbeds.jenkins.pipelines.templates.metadata

import com.hotelbeds.jenkins.pipelines.templates.Template


class MetadataTemplate implements Template {

    def static final METADATA_TEMPLATE = '''
jenkinsPath: ${metadata.jenkinsPath}
scmRepository: ${metadata.scmRepository}
scmBranch: ${metadata.scmBranch}
technology: ${metadata.technology}
releasementStrategy: ${metadata.releasementStrategy}
'''

    /* (non-Javadoc)
     * @see com.hotelbeds.jenkins.pipelines.templates.Template#getTemplate()
     */

    @Override
    public Object getTemplate() {
        return METADATA_TEMPLATE
    }
}
