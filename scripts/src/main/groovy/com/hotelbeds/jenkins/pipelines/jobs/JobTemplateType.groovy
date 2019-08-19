package com.hotelbeds.jenkins.pipelines.jobs

import com.hotelbeds.jenkins.pipelines.helper.ArgoCDTemplateHelper
import com.hotelbeds.jenkins.pipelines.helper.CITemplateHelper
import com.hotelbeds.jenkins.pipelines.helper.JobTemplateHelper
import com.hotelbeds.jenkins.pipelines.helper.PackageDockerTemplateHelper

enum JobTemplateType {

    ARGOCD( ['environment'], new ArgoCDTemplateHelper() ),
    CI( ['buildCommand', 'utCommand', 'itCommand', 'analysisCommand'], new CITemplateHelper()),
    PACKAGE_DOCKER( [], new PackageDockerTemplateHelper() )


    def requiredParameters
    JobTemplateHelper templateHelper

    private JobTemplateType(Object requiredParameters, Object templateHelper) {
        this.requiredParameters = requiredParameters
        this.templateHelper = templateHelper
    }

}

