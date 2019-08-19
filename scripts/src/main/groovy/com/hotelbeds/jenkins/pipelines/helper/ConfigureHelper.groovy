package com.hotelbeds.jenkins.pipelines.helper

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.jobs.AceJob
import com.hotelbeds.jenkins.pipelines.jobs.FreeStyleJob
import com.hotelbeds.jenkins.pipelines.jobs.Job
import com.hotelbeds.jenkins.pipelines.jobs.JobStatus
import com.hotelbeds.jenkins.pipelines.jobs.MavenJob
import com.hotelbeds.jenkins.pipelines.jobs.tech_versions.DockerImage
import com.hotelbeds.jenkins.pipelines.jobs.tech_versions.JavaJDK
import com.hotelbeds.jenkins.pipelines.jobs.NexusJob
import com.hotelbeds.jenkins.pipelines.jobs.NpmJob
import com.hotelbeds.jenkins.pipelines.jobs.PythonJob
import com.hotelbeds.jenkins.pipelines.jobs.tech_versions.Python
import com.hotelbeds.jenkins.pipelines.jobs.RdmApprovalJob
import com.hotelbeds.jenkins.pipelines.jobs.RundeckJob
import com.hotelbeds.jenkins.pipelines.jobs.DatadogConfig

class ConfigureHelper {

    /**
     * Configure build name setter.
     *
     * @return the closure
     */
    static Closure configureBuildNameSetter(name = '#${BUILD_ID} for ${BUILD_VERSION}') {
        return { project ->
            project / 'buildWrappers' / 'org.jenkinsci.plugins.buildnamesetter.BuildNameSetter' {
                template(name)
                runAtStart(true)
                runAtEnd(true)
            }
        }
    }

    /**
     * Configure stash repo.
     *
     * @param repoUrl the repo url
     * @param repoBranch the repo branch
     * @return the closure
     */
    static Closure configureStashRepo(String repoUrl, String repoBranch, String directory = null) {
        return {
            git {
                remote {
                    url(repoUrl)
                    credentials(Constants.SSH_KEY_ID)
                }
                branch(repoBranch)
                extensions {
                    cleanBeforeCheckout()
                    if (directory != null) {
                        relativeTargetDirectory(Constants.DEFAULT_PROJECT_DIR)
                    }
                }
            }
        }
    }

    /**
     * Configure pipeline scripts repo.
     *
     * @return the closure
     */
    static Closure configurePipelineScriptsRepo() {
        return {
            git {
                remote {
                    url(Constants.PIPELINES_SCRIPTS_REPO)
                    credentials(Constants.SSH_KEY_ID)
                }
                branch(Constants.PIPELINES_SCRIPTS_BRANCH)
                extensions { relativeTargetDirectory(Constants.DEFAULT_SCRIPTS_DIR) }
                configure { node ->
                    node / 'extensions' / 'hudson.plugins.git.extensions.impl.PathRestriction' { excludedRegions('.*') }
                }
            }
        }
    }

    /**
     * Configure shared artifacts.
     *
     * @param job the job
     * @return the closure
     */
    static Closure configureSharedArtifacts(Job job) {
        return { project ->
            job.sharedArtifacts.each { sharedArtifact ->
                Job targetJob = job.stage.pipeline.findJobByName(sharedArtifact.jobName)
                String artifactPrefixPath = Constants.DEFAULT_PROJECT_DIR + "/"
                if (targetJob == null) {
                    throw new Error("$sharedArtifact.jobName does not exist.")
                }
                project / 'properties' / 'hudson.plugins.copyartifact.CopyArtifactPermissionProperty' {
                    projectNameList { 'string'(targetJob.getJenkinsJobName()) }
                }
                project / 'publishers' / 'hudson.tasks.ArtifactArchiver' {
                    'artifacts'(artifactPrefixPath + sharedArtifact.artifacts.join("," + artifactPrefixPath))
                    'onlyIfSuccessful'(true)
                }
            }
        }
    }

    /**
     * Configure imported artifacts.
     *
     * @param job the job
     * @return the closure
     */
    static Closure configureImportedArtifacts(Job job) {
        return {
            steps {
                if (job.stage.pipeline.sharedArtifacts.containsKey(job.name)) {
                    job.stage.pipeline.sharedArtifacts.get(job.name).each { fromJobName, artifacts ->
                        Job fromJob = job.stage.pipeline.findJobByName(fromJobName)
                        String artifactPrefixPath = Constants.DEFAULT_PROJECT_DIR + "/"
                        copyArtifacts(fromJob.getJenkinsJobName()) {
                            includePatterns(artifactPrefixPath + artifacts.join().toString().replaceAll("\\[|\\]", "").split(",")*.trim().join(", " + artifactPrefixPath))
                            buildSelector { latestSuccessful(true) }
                        }
                    }
                }
            }
        }
    }


    /**
     * Configure trigger.
     *
     * @param job the job
     * @param nextJobs the next jobs
     * @param parameters the parameters
     * @return the closure
     */
    static Closure configureTrigger(Job job, nextJobs, parameters) {
        def manualTriggerJobs = []
        def triggerJobs = []

        nextJobs.each { Job nextJob ->
            if (nextJob?.manualTrigger) {
                manualTriggerJobs << nextJob.getJenkinsJobName()
            } else {
                triggerJobs << nextJob.getJenkinsJobName()
            }
        }

        return { project ->
            if (!manualTriggerJobs.empty) {
                project / 'publishers' / 'au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger' {
                    'downstreamProjectNames'(manualTriggerJobs.join(','))
                    configs {
                        'hudson.plugins.parameterizedtrigger.PredefinedBuildParameters' {
                            'properties'(parameters.collect { key, value -> "$key=$value" }.join("\n"))
                        }
                    }
                }
            }
            if (!triggerJobs.empty) {
                project / 'publishers' / 'hudson.plugins.parameterizedtrigger.BuildTrigger' / 'configs' / 'hudson.plugins.parameterizedtrigger.BuildTriggerConfig' {
                    projects(triggerJobs.join(','))
                    condition(job.triggerCondition.name())
                    triggerWithNoParameters(false)
                    configs {
                        'hudson.plugins.parameterizedtrigger.PredefinedBuildParameters' {
                            'properties'(parameters.collect { key, value -> "$key=$value" }.join("\n"))
                        }
                    }
                }
            }
        }
    }

    /**
     * Configure node JS.
     *
     * @return the closure
     */
    static Closure configureNodeJS() {
        return { project ->
            project / 'buildWrappers' / 'jenkins.plugins.nodejs.tools.NpmPackagesBuildWrapper' {
                'nodeJSInstallationName'('default')
            }
        }
    }

    /**
     * Configure update build name.
     *
     * @param job the job
     * @return the closure
     */
    static Closure configureUpdateBuildName(Job job) {
        return { project ->
            def preffix = job instanceof MavenJob ? 'prebuilders' : 'builders'
            project / preffix / 'org.jenkinsci.plugins.buildnameupdater.BuildNameUpdater' {
                'macroTemplate'(job.firstJobOfPipeline ? '#${PROPFILE,file="' + Constants.DEFAULT_PROJECT_DIR + '/version.properties",property="BUILD_VERSION"}' : '#${BUILD_ID} for #${BUILD_VERSION}')
                'fromFile'(false)
                'fromMacro'(true)
                'macroFirst'(false)
                'buildName'('version.txt')
            }
        }
    }

    /**
     * Configure env inject builder.
     *
     * @param job the job
     * @return the closure
     */
    static Closure configureEnvInjectBuilder(Job job) {
        return { project ->
            def preffix = job instanceof MavenJob ? 'prebuilders' : 'builders'
            project / preffix / 'EnvInjectBuilder' / 'info' {
                'propertiesFilePath'(Constants.DEFAULT_PROJECT_DIR + '/version.properties')
            }
        }
    }

    /**
     * Configure default Datadog tags.
     *
     * @param job the job
     */
    static configureDefaultDatadogTags(Job job) {
        if (job.datadog == null || job.datadog?.tags == null) {
            job.datadog = new DatadogConfig()
            job.datadog?.tags = []
        }

        processCommonDatadogTags(job)

        switch (job) {
            case FreeStyleJob: break
            case MavenJob: break
            case NexusJob:
                processDatadogTagsForNexusJob(job)
                break
            case NpmJob: break
            case RdmApprovalJob: break
            case RundeckJob: break
            case AceJob: break
            case PythonJob: break
            default: break
        }

    }

    /**
     * Add Datadog tag
     *
     * @param job
     * @param tagName
     * @param tagValue
     */
    private static void addDatadogTag(Job job, String tagName, String tagValue) {
        job.datadog?.tags << "jenkins_" + tagName + "=" + tagValue
    }

    /**
     * Process common Datadog tags for all jobs.
     *
     * @param job
     */
    private static void processCommonDatadogTags(Job job) {

        def profileTagValue
        if (isCollectionOrArray(job?.profiles)) {
            profileTagValue = job?.profiles.join(",")
        } else {
            profileTagValue = job?.profiles
        }

        addDatadogTag(job, "profile", profileTagValue.toString())
        addDatadogTag(job, "releasement_strategy", job?.stage?.pipeline?.releasementStrategy)
        addDatadogTag(job, "pipeline", job?.stage?.pipeline?.name)
        addDatadogTag(job, "stage", job?.stage?.name)
        addDatadogTag(job, "technology", job?.stage?.pipeline?.technology)
        addDatadogTag(job, "create_rdm_jira_ticket", String.valueOf(job?.createRdmJiraTicket))
        addDatadogTag(job, "job", job?.name)
        addDatadogTag(job, "jdk_version", job?.stage?.pipeline?.jdkVersion)
    }

    /**
     * Check if the object is a Collection or Array
     *
     * @param object
     * @return
     */
    private static boolean isCollectionOrArray(object) {
        [Collection, Object[]].any { it.isAssignableFrom(object.getClass()) }
    }

    /**
     * Process Datadog tags for Nexus jobs.
     *
     * @param job
     */
    private static void processDatadogTagsForNexusJob(NexusJob job) {
        final String REPO_SUFFIX = "-repo"

        String nexusEnvironment = job?.environment
        String repoId = job?.nexusRepositoryId
        def businessLocation = nexusEnvironment[nexusEnvironment.indexOf('-') + 1..nexusEnvironment.length() - 1]
        def environment = repoId[0..repoId.indexOf('-') - 1]
        def business = repoId[repoId.indexOf('-') + 1..repoId.indexOf('-', repoId.indexOf('-') + 1) - 1]
        def location = (businessLocation - business).minus("-")
        def environmentBusinessLocation = environment + "-" + businessLocation + "-"
        def solutionRepo = repoId - environmentBusinessLocation
        def indexRepo = (solutionRepo).findLastIndexOf({ REPO_SUFFIX }) - REPO_SUFFIX.length()
        def solution = solutionRepo.substring(0, indexRepo + 1)

        addDatadogTag(job, "environment", environment)
        addDatadogTag(job, "business", business)
        addDatadogTag(job, "location", location)
        addDatadogTag(job, "solution", solution)
    }

    /**
     * Configure datadog tags.
     *
     * @param job the job
     * @return the closure
     */
    static Closure configureDatadogTags(Job job) {
        return { project ->
            project / 'properties' / 'org.datadog.jenkins.plugins.datadog.DatadogJobProperty' {
                'tagProperties'(job?.datadog?.tags?.collect { tag -> "$tag" }.join("\n"))
                'emitOnCheckout'(false)
            }
        }
    }

    /**
     * Configure datadog tags for regenerator job.
     *
     * @param job the job
     * @return the closure
     */
    static Closure configureDatadogTagsForRegenerator(pipelineName) {
        return { project ->
            def tags = ['jenkins_pipeline=' + pipelineName, 'jenkins_stage=REGENERATOR', 'jenkins_job=REGENERATOR']

            project / 'properties' / 'org.datadog.jenkins.plugins.datadog.DatadogJobProperty' {
                'tagProperties'(tags.join("\n"))
                'emitOnCheckout'(false)
            }
        }
    }


    /**
     * Configure artifact resolver.
     *
     * @param _groupId the group id
     * @param _artifactId the artifact id
     * @param _version the version
     * @param _extension the extension
     * @return the closure
     */
    static Closure configureArtifactResolver(String _groupId, String _artifactId, String _version, String _extension) {
        return { project ->
            project / 'builders' / 'org.jvnet.hudson.plugins.repositoryconnector.ArtifactResolver' {
                'failOnError'(true)
                'artifacts' {
                    'org.jvnet.hudson.plugins.repositoryconnector.Artifact' {
                        'groupId'(_groupId)
                        'artifactId'(_artifactId)
                        'version'(_version)
                        'extension'(_extension)
                    }
                }
            }
        }
    }

    /**
     * Configure Inject Global Passwords.
     *
     * @return the closure
     */
    static Closure configureInjectGlobalPasswords() {
        return { project ->
            project / 'buildWrappers' / 'EnvInjectPasswordWrapper' {
                injectGlobalPasswords(true)
                maskPasswordParameters(true)
                passwordEntries()
            }
        }
    }


    /**
     * Configure Rdm Jira Ticket Step.
     *
     * @param job the job
     * @param serviceName the serviceName
     * @param environmentName the environmentName
     * @param rdmStandardChange , issue Type
     * @return the closure
     */
    static Closure configureRdmJiraTicketPublish(Job job, String serviceName, String environmentName, boolean rdmStandardChange) {
        if (job.triggerCondition == JobStatus.ALWAYS) {
            def final_status = 'always'
            return configureRdmJiraTicketByEmail(job, serviceName, environmentName, final_status, rdmStandardChange)
        } else {
            def final_status = 'success'
            return configureRdmJiraTicketByEmail(job, serviceName, environmentName, final_status, rdmStandardChange)
        }
    }


    static Closure configureRdmJiraTicketByEmail(Job job, String serviceName, String environmentName, String final_status, boolean rdmStandardChange) {
        return {
            def pattern = ~"job\\/[^/]+\$"
            def pipelineUrl = (job.getJenkinsJobUrl() - pattern) + "view/${serviceName}/"
            def now = new Date().format("dd/MM/yyyy")
            def issueType
            def rdmEmail
            if (rdmStandardChange) {
                issueType = "Standard Change"
                rdmEmail = "rdmautomatedtickets@hotelbeds.com"
            } else {
                issueType = "Planned Change"
                rdmEmail = "rdmautomatedticketspc@hotelbeds.com"
            }
            extendedEmail {
                recipientList("${rdmEmail}")
                defaultSubject("Deploy ${environmentName} for ${serviceName} \${BUILD_VERSION}")
                defaultContent("""
project key: RDM
issuetype: ${issueType}
labels: jenkins-pipeline
reporter: rdmdeploy
Impacted Technology (RDM): id=25204
Requesting Area (RDM): id=24426
Risk: id=13310
Rollback plan: Install previous version
Artifact Type: id=14120
summary: Deploy ${environmentName} for ${serviceName} \${BUILD_VERSION}
description: Deploy ${environmentName} for ${serviceName} \${BUILD_VERSION}
components: ${serviceName}
Planned Start: ${now}
Planned End: ${now}
Implementation Steps: Please go to: ${pipelineUrl} to approve the deploy for ${environmentName} ${serviceName} \${BUILD_VERSION}

Deployment details: ${serviceName} \${BUILD_VERSION}
				""")
                contentType('text/plain')
                triggers {
                    "${final_status}" {
                        sendTo {
                            recipientList()
                        }
                    }
                }
            }
        }
    }

    /**
     * Configure env inject.
     *
     * @param groovyScript the groovy script
     * @return the closure
     */
    static Closure configureEnvInject(groovyScript) {
        return { project ->
            project / 'buildWrappers' / 'EnvInjectBuildWrapper' / 'info' {
                groovyScriptContent(groovyScript)
            }
        }
    }

    /**
     * Configure auto approval scriptlet.
     *
     * @return the closure
     */
    static Closure configureAutoApprovalScriptlet() {
        def approvePrefixParam = {
            name 'approvalPrefix'
            value Constants.APPROVE_PREFFIX_PARAM_VALUE
        }

        return { project ->
            project / 'builders' << 'org.jenkinsci.plugins.scriptler.builder.ScriptlerBuilder' {
                builderId(Constants.AUTOAPPROVED_GROOVY_SCRIPTS_BUILDER_ID_GRABBED_FROM_XML)
                scriptId('autoapproved-groovy-scripts.groovy')
                propagateParams(false)
                parameters {
                    'org.jenkinsci.plugins.scriptler.config.Parameter' approvePrefixParam
                }
            }
        }
    }

    /**
     * Configure read only parameter.
     *
     * @param groovyScript the groovy script
     * @return the closure
     */
    static Closure configureReadOnlyParameter(parameterName, parameterValue) {
        return { project ->
            project / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' / 'com.wangyin.ams.cms.abs.ParaReadOnly.WReadonlyStringParameterDefinition' {
                name(parameterName)
                defaultValue(parameterValue)
            }
        }
    }

    /**
     * Configure extended choice parameter.
     *
     * @param jobName the job name
     * @param nameParameter the name parameter
     * @param typeParameter the type parameter
     * @param groovyScriptParameter the groovy script parameter
     * @return the closure
     */
    static Closure configureExtendedChoiceParameter(jobName, nameParameter, typeParameter, groovyScriptParameter, delimeter = ',', visibleItemCountParameter = 5) {
        return { project ->
            project / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' / 'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                visibleItemCount(visibleItemCountParameter)
                type(typeParameter)
                multiSelectDelimiter(delimeter)
                name(nameParameter)
                projectName(jobName)
                groovyScript(groovyScriptParameter)
            }
        }
    }

    /**
     * Configure extended choice parameter.
     *
     * @param paramName the param name
     * @param visibleItems the visible items
     * @param paramType the param type
     * @param paramValue the param value
     * @param paramDefaultValue the param default value
     * @param paramDelimiter the param delimiter
     * @return the closure
     */
    static Closure configureExtendedChoiceParameter(String paramName, int visibleItems, String paramType, String paramValue, String paramDefaultValue,
                                                    String paramDelimiter) {
        return { Node project ->
            project / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' / 'com.cwctravel.hudson.plugins.extended__choice__parameter.ExtendedChoiceParameterDefinition' {
                name(paramName)
                description()
                quoteValue(false)
                saveJSONParameterToFile(false)
                visibleItemCount(visibleItems)
                type(paramType)
                value(paramValue)
                defaultValue(paramDefaultValue)
                multiSelectDelimiter(paramDelimiter)
            }
        }
    }

    /**
     * Gets bash command to execute script with parameters
     *
     * @param scriptFileName the scriptFileName
     * @param params the params for bash script
     * @return bash command to execute
     */
    static String bashCommand(String scriptFileName, String... params) {
        return 'bash ' + Constants.SCRIPTS_PATH + scriptFileName + ' ' + params.join(' ')
    }

    /**
     * Gets script content from workspace file
     *
     * @param dslFactory
     * @param scriptFileName
     * @return the script content
     */
    static String getScript(def dslFactory, String scriptFileName, Map<String, String> params = null) {
        String script = dslFactory.readFileFromWorkspace(Constants.SCRIPTS_PATH + scriptFileName)
        if (params != null) {
            params.each { param, value ->
                script = script.replaceAll(param, value)
            }
        }
        return script
    }

    /**
     * Gets JDK Version
     *
     * @param jdkVersionCode
     * @return the JDK code
     */
    static String getJdkVersion(def jdkVersionCode) {
        String jdkCode
        if (jdkVersionCode == JavaJDK.JDK_18_U161.code) {
            jdkCode = JavaJDK.JDK_18_U161.code
        } else if (jdkVersionCode == JavaJDK.JDK_18_U192.code) {
            jdkCode = JavaJDK.JDK_18_U192.code
        } else if (jdkVersionCode == JavaJDK.JDK_11_0_2u9.code) {
            jdkCode = JavaJDK.JDK_11_0_2u9.code
        } else {
            //Default version
            jdkCode = JavaJDK.JDK_18_U45.code
        }
        return jdkCode
    }

    /**
     * Gets Python Version
     *
     * @param pythonVersionCode
     * @return the Python version
     */
    static Python getPythonVersion(def pythonVersionCode) {
        Python pythonCode
        if (pythonVersionCode == Python.PYTHON36.code) {
            pythonCode = Python.PYTHON36
        } else {
            //Default version
            pythonCode = Python.PYTHON2
        }
        return pythonCode
    }


    /**
     * Configure virtualenv python step
     *
     * @param command the command
     * @return the closure
     */
    static Closure configureVirtualEnvPythonStep(String stepCommand, String pipelineName, Python pythonVersion) {
        return {
            virtualenv {
                name('venv-' + pipelineName)
                pythonName(pythonVersion.name)
                command('cd ' + Constants.DEFAULT_PROJECT_DIR + ' && ' + pythonVersion.code + ' ' + stepCommand)
                clear(false)
            }
        }
    }


    /**
     * Install awscli in virtualenv
     *
     * @return the closure
     */
    static Closure configureVirtualEnvPythonAWSCli(String pipelineName, Python pythonVersion) {
        return {
            virtualenv {
                name('venv-' + pipelineName)
                pythonName(pythonVersion.name)
                command('cd ' + Constants.DEFAULT_PROJECT_DIR + ' && pip install --upgrade awscli')
                clear(false)
            }
        }
    }

    /**
     * Install awscli in virtualenv
     *
     * @return the closure
     */
    static Closure configureVirtualEnvPythonHbgSetuptools(String pipelineName, Python pythonVersion) {
        return {
            virtualenv {
                name('venv-' + pipelineName)
                pythonName(pythonVersion.name)
                command('cd ' + Constants.DEFAULT_PROJECT_DIR
                        + ' && ' + pythonVersion.code
                        + ' -m pip install -U -e git+http://stash.hotelbeds/scm/~fsimo/py-setuptools-hbg.git#egg=setuptools-hbg')
                clear(false)
            }
        }
    }

}
