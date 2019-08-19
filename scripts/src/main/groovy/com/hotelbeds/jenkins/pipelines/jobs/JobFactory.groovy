package com.hotelbeds.jenkins.pipelines.jobs

/**
 * A factory for creating Job objects.
 */
class JobFactory {

    /**
     * Gets the single instance of JobFactory.
     *
     * @param config the config
     * @return single instance of JobFactory
     */
    static def getInstance(config) {
        JobType jobType = JobType.valueOf(config.type.toUpperCase())
        switch (jobType) {
            case JobType.FREE:
                return new FreeStyleJob(config)
                break
            case JobType.MAVEN:
                return new MavenJob(config)
                break
            case JobType.NEXUS:
                return new NexusJob(config)
                break
            case JobType.NPM:
                return new NpmJob(config)
                break
            case JobType.RDM_APPROVAL:
                return new RdmApprovalJob(config)
                break
            case JobType.RUNDECK:
                return new RundeckJob(config)
                break
            case JobType.ACE:
                return new AceJob(config)
                break
            case JobType.PYTHON:
                return new PythonJob(config)
                break
            case JobType.DOCKER:
                return new DockerJob(config)
                break
        }
    }
}
