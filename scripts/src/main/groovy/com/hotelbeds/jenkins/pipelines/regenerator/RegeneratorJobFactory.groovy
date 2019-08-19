package com.hotelbeds.jenkins.pipelines.regenerator

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Pipeline

/**
 * A factory for creating Regenerator Job objects.
 */
class RegeneratorJobFactory {

    /**
     * Gets the single instance of RegeneratorJobFactory.
     *
     * @param pipeline the pipeline
     * @param pipelineFileName the pipeline file name
     * @return single instance of RegeneratorJobFactory
     */
    static def getInstance(Pipeline pipeline, String pipelineFileName) {
        switch (pipeline.releasementStrategy) {
            case Constants.CONTINUOUS_DEPLOYMENT_RELEASEMENT_STRATEGY:
                return new RegeneratorContinuousDeploymentStrategyJob(pipeline, pipelineFileName)
                break
            case Constants.RELEASES_RELEASEMENT_STRATEGY:
                return new RegeneratorReleasesStrategyJob(pipeline, pipelineFileName)
                break
        }
    }
}
