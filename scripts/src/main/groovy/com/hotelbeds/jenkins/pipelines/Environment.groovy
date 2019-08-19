package com.hotelbeds.jenkins.pipelines


enum Environment {

    SHARED('shared'),
    DEV('dev'),
    TEST('test'),
    STAGE('stage'),
    LIVE('live'),
    INT('int')

    def code

    private Environment(Object code) {
        this.code = code
    }

    /**
     * Find shared environment.
     *
     * @param profile the profile
     * @return the environment
     */
    static String findSharedEnvironment(profile, environmentName) {
        Environment _environment = Environment.valueOf(environmentName)
        String environmentId = _environment.code
        return SHARED.code + Profile.SEPARATOR + Profile.valueOf(profile).getNexusDefinition()
    }

    static String findDeploymentEnvironmentCode(String profile, String environmentName) {
        Environment _environment = Environment.valueOf(environmentName)
        String environmentId = _environment.code
        return Profile.valueOf(profile).getRundeckEnvironmentDefinition(environmentId)
    }

    static String findDeploymentEnvironmentCodeForPythonFunctions(String profile, String environmentName) {
        Environment _environment = Environment.valueOf(environmentName)
        String environmentId = _environment.code
        return environmentId + Profile.SEPARATOR + Profile.valueOf(profile).getNexusDefinition()
    }

    /**
     /**
     * Checks if is live.
     *
     * @param environment the environment
     * @return true, if is live
     */
    static boolean isLive(String environment) {
        Environment _environment = Environment.valueOf(environment)
        return _environment == LIVE
    }

    /**
     * Checks if is test.
     *
     * @param environment the environment
     * @return true, if is test
     */
    static boolean isTest(String environment) {
        Environment _environment = Environment.valueOf(environment)
        return _environment == TEST
    }

    /**
     * Checks if is dev.
     *
     * @param environment the environment
     * @return true, if is dev
     */
    static boolean isDev(String environment) {
        Environment _environment = Environment.valueOf(environment)
        return _environment == DEV
    }

    /**
     * Checks if is stage or test in cloud profiles.
     *
     * @param environment the environment
     * @return true, if is stage
     */
    static boolean isStage(String environment) {
        Environment _environment = Environment.valueOf(environment)
        return _environment == STAGE
    }

    /**
     * Checks if is int cloud profiles.
     *
     * @param environment the environment
     * @return true, if is int
     */
    static boolean isInt(String environment) {
        Environment _environment = Environment.valueOf(environment)
        return _environment == INT
    }
}
