# Pipelines

- [Pipelines](#pipelines)
  - [pipeline.yml configuration](#pipelineyml-configuration)
    - [Pull-requests builder](#pull-requests-builder)
- [Stages](#stages)
- [ConfigRepos](#configrepos)
- [Jobs](#jobs)
- [Meta-information file {service_name}.yml](#meta-information-file-servicenameyml)

The pipeline of a service will be created from two configuration files. One of them, called `{service_name}.yml`, is stored in the Bitbucket **jenkins-pipelines** repository where all the meta-information that the Delivery Engineering team needs to manage the pipelines will be saved. The other file, which is called `pipeline.yml`, is in the same repository of the application and will contain the configuration and pipeline execution flow.

## pipeline.yml configuration

The pipeline is defined by the `pipeline.yml` file that is located in the application repository.

The configuration attributes of the pipeline are described below. The specifics of the different jobs supported by pipelines can be found in **Jobs**.
> **Important**
>
> Attributes with asterisks are mandatory.
> Between braces {} the type of the attribute is indicated.
> Pipelines are key sensitive. Respect the case of the values that appear in the lists of possible values.

- `name* {String}`: name of the service. It is the one that will name the pipeline tab and will be used as a prefix for the construction of all jobs that are part of the pipeline.
- `packageType* {String}`: Type of artifact that is going to be generated. List of possible values:
  - **rpm**: default value.
  - **zip**: valid only for lambda java functions, python functions or AngularJS.
- `recipients {String}`: e-mail addresses to which notifications will be sent in case the job execution fails.
- `orchestrate {Boolean}`: possible values:
  - **true**: default value. Pipeline execution will be orchestrated from Jenkins.
  - **false**: only the jobs will be generated, not the execution flow.
- `profiles* {List}`: List of deployment profiles that have this pipeline.
- `stages* {List}`: list of stages that make up the pipeline. In the following section is explained in more detail.
- `configRepos {List}`: list of frontend application configuration repositories to upload to an S3 bucket. In the corresponding section, it is explained in more detail.
- `generateRdmTicket {Boolean}`: enable or disable the creation of JIRA rdm task before RdmApproval step.
- `rdmStandardChange {Boolean}`: configure RDM issue type as Standard Change. By default it will be a Planned Change.
- `jdkVersion {String}`: attribute where you can specify which version of jdk would be used. Valid values:
  - **JDK_1.8_u45**: default version that is used when attribute jdkVersion is empty or not specified.
  - **JDK_1.8_u161**
  - **JDK_1.8_u192**
  - **OpenJDK_11.0.2u9**
- `nodejsVersion {String}`: attribute to set the NodeJS version to use. List of possible values:
  - **NodeJS 8.9.4**: default version that is used when attribute nodejsVersion is empty or not specified.
  - **NodeJS 10.4.0**
- `pythonVersion {String}`: attribute where you can specify which version of python would be used. Valid values:
  - **python2.7**: default version that is used when attribute pythonVersion is empty or not specified.
  - **python3.6**

### Pull-requests builder

- `pullRequestBuilder {Map}`: Enables the build for pull-requests. Generates a view called PR in the pipeline with a job that tests whether the merge over master is valid. **Only available for Springboot pipelines at the moment**.
- `mode {String}`:  Indicates the build mode for the pull-request. If the mode is not indicated, the PR view and the job will not be generated. List of possible values:
  - **SECURE**: Compile and execute unit tests and integration tests with the indicated profiles.
  - **NINJA**: Only compile and execute unit tests.
  - **KAMIKAZE_MONKEY**: Only compile.
  > **Warning**
  >
  > To change from one mode to another, you must take into account that it will only be applied to pull-requests created or updated after making the change in the pipeline.yml, since the job PR_BUILDER is destroyed and recreated when the job REGENERATOR is executed, in charge of processing the changes in the pipeline.yml.
- `generatePullRequestChecker {Boolean}`: Enables or disables the continuous integration of pull requests. Tests will be performed on the result of doing merge fast forward on master.
- `codeAnalysis {Boolean}`: Attribute to disable code analysis with SonarQube on modified files. It will not publish results in SonarQube, only print a report in the log. PR analysis isn't about making anything fail. Instead, it's about allowing the pull request reviewer to be fully informed in making that review. Default: true
- `profiles {String}`: list of deployment profiles that have this pipeline. If not indicated **'dev,ci'** will be used.

# Stages

The stages are the boxes where the jobs are grouped in the Jenkins pipeline. They only have a visual function.

- `name*{String}`: name of the stage. It is the one that will name the box of the pipeline of Jenkins.
- `jobs*{List}`: list of jobs that make up that stage. For a more detailed view see **Jobs**.

# ConfigRepos

The config repos are independent jobs from the pipeline workflow. Its function is to upload the content of the repository configured in each job to AWS S3 when a commit is detected in the master's branch.

Being independent, they will not be displayed in the pipeline view since they are not part of the CI / CD of the application.

- `name* {String}`: name of the job that will be created to publish the application configuration in S3. It must be a unique name.
- `repositoryUrl* {String}`: url of the Git repository of application configuration.
- `environment* {String}`:  name of the environment in which the S3 bucket is located.
- `s3bucket* {String}`: name of the S3 bucket where to display the contents of the indicated repository.

# Jobs

These are the types of jobs currently supported by the pipeline.yml file.

> **Important**
>
> Attributes with asterisks are mandatory.
> Between braces {} the type of the attribute is indicated.
> Pipelines are key sensitive. Respect the case of the values that appear in the lists of possible values.

- ## Job
  It is the base job, from which all other jobs inherit. In other words, all attributes of Job are available in the rest of Jobs. Attributes that have validity in a job type will be explicitly commented on the particular job type.

  - `name* {String}`:  It is the name of the job within the pipeline. The name attribute will be used as a suffix in the Jenkins job, since the name of the job within jenkins will be a composition of `{pipeline.name} _ {job.name}`. The name value must be unique throughout the pipeline, if it is repeated, the settings will be overwritten.

    ```yml
    pipeline:
      name: aif2-gen
      profiles:
        - DPS
      stages:
        - name: BUILD
          type: maven
          profiles:
            - DPS
          showName: Build
          jenkinsAgentTags: DPS
          command: clean compile -X -e -U
    ...
    ```

    ![Example of job name in jenkins][jobName]

  - `showName {String}`: is the name that will appear in the Jenkins delivery pipeline. If not indicated, it will default to Jenkins' job name.

    ```yml
    ...
        - name: BUILD
          type: maven
          profiles:
            - DPS
          showName: Build
          jenkinsAgentTags: DPS
          command: clean compile -X -e -U
    ...
    ```

    ![Example of job show name in jenkins][jobShowName]
  - `type* {String}`:  job type, must be one of the types allowed. Depending on the value of this attribute, the type of Jenkins job you create will change.
  List of possible values:
    - **maven**: for jobs of type Maven.
    - **python**: for jobs of type Python.
    - **nexus**: to upload binaries to Nexus Sonatype.
    - **npm**: jobs of type npm.
    - **rdm_approval**: to put manual checks for RDM approval of a deployment.
    - **rundeck**:  to run Rundeck jobs.
    - **ace**: specific job for ACE compilations.
    - **docker**: to run specific jobs with docker images
    - **template**: to use specific template to simplify pipeline creation
  - `jenkinsAgentTags* {String}`: a list separated by logical ORs (||) from the Jenkins node tags wherever we want the job to run. It is possible to indicate more than one tag, if it does, ON_PREMISE takes precedence over the others.
  List of possible values:
    - **acedaemon**: will run on the node optimized for ACE daemon compilation in DEV.
    - **acedaemonTEST**: will run on the node optimized for ACE daemon compilation in TEST.
    - **ON_PREMISE**: will run on the Jenkins nodes found in Mirall.
    - **AWS**: will run on the Jenkins nodes found in the Amazon HBG account.
    - **DPS**: will be cast on the Jenkins nodes found in the Amazon DPS account.

  - `profiles* {List}`: The value of this attribute defines which pipeline deployment stream the job will run on. When we say a job to a job, we are adding it to the execution flow of the deployment profile. The execution flow goes from top to bottom, that is, in two jobs that are in the same deployment flow, the one that is above in the pipeline will be executed first.
  List of possible values:
    - **ON_PREMISE**: adds the job to the ON_PREMISE deployment workflow.
    - **AWS**:  adds the job to the AWS deployment workflow.
    - **DPS**: adds the job to the DPS deployment workflow.
    - **AWS_US_EAST_1**: adds the job to the AWS_US_EAST_1 deployment workflow.
    - **AWS_AP_SOUTHEAST_1**: adds the job to the AWS_AP_SOUTHEAST_1 deployment workflow.

    ![Example of pipeline flow with profiles][flow]

  - `publishers {Map}`: attribute to configure publishers of Jenkins jobs.
    - `junit {List}`: attribute to publish the results of executed JUnit tests. In the list you must indicate the different reports of the tests.
    - `testng {List}`: attribute to publish the results of JUnit tests executed. In the list you must indicate the different reports of the tests.
    - `allure {Map}`:
      - `path {String}`: attribute to configure the path of Allure reports.
      - `version {String}`: attribute to set the Allure version. Possible values:
        - **allure1**
        - **allure2**
    - `html {Map}`:
      - `reportFile {String}`: the file(s) to provide links inside the report directory (`index.html`)
      - `reportPath {String}`: the path to the HTML report directory relative to the workspace. (`target/reports/htmlReports`)
      - `reportTitle {String}`: the name of the report.
  - `manualTrigger {Boolean}`: this Boolean attribute allows us to set the trigger as manual when its value is true. In other words, if you mark a job with a manual trigger, jobs that are triggered by that job will appear in the pipeline with a play just like the RDM_APPROVAL jobs.
  - `triggerCondition {String}`: This attribute allows us to indicate the threshold to be exceeded by a job to continue with the execution of the pipeline. (Only valid when next jobs is not of type `manualStep`).
  List of possible values:
    - **ALWAYS**: launches the following jobs every time.
    - **FAILED**: launches the following jobs if the job has failed.
    - **FAILED_OR_BETTER**: launches the following jobs if the result is failure or better.
    - **SUCCESS**: launches the following jobs only if the job is successful.
    - **UNSTABLE**: launches the following jobs if the job has ended with unstable output.
    - **UNSTABLE_OR_BETTER**: launches the following jobs if the job has ended up unstable or better.
    - **UNSTABLE_OR_WORSE**:  launches the following jobs if the job has ended up unstable or worse.
  - `scm {Map}`: This attribute allows us to overwrite the default repository for the pipeline and have the job pull that repository instead of the default repository. Currently only Git repositories are supported.
    - `url {String}`:  url of the code Git repository.
    - `branch {String}`: branch with which we want the job to work. You can also specify concrete commits instead of a branch.
  - `datadog {Map}`: this attribute allow us to add datadog tags to job.
    - `tags {List}`: list of tags that we want to tag to the job. Tags have the structure `name=value`. By default, these tags are added:
      - **jenkins_profile**: pipeline profile. See profiles previously.
      - **jenkins_releasement_strategy**: conitinuous deployment or releases.
      - **jenkins_pipeline**: pipeline name.
      - **jenkins_stage**: name of the stage to which the job belongs.
      - **jenkins_technology**: type of pipeline technology:
        - Spring Boot
        - AngularJS
        - Java Function
        - Python function
        - ACE
        - Python
      - **jenkins_create_rdm_jira_ticket**.
      - **jenkins_job**: default name of the job.
      - **jenkins_jdk_version**: JDK version.
    
    Example: 
   
    ```yml
    ...
    datadog:
      tags:
        - jenkins_profile=ON_PREMISE
        - jenkins_releasement_strategy=CONTINUOUS_DEPLOYMENT
        - jenkins_pipeline=nights-watch-pipeline
        - jenkins_stage=BUILD
        - jenkins_technology=Spring Boot
        - jenkins_create_rdm_jira_ticket=false
        - jenkins_job=BUILD
        - jenkins_jdk_version=JDK_1.8_u192
    ...
    ```

- ## Job Maven
  - `command {String}`: attribute that indicates the Maven goals you want to execute. Important, do not put the mvn.

- ## Job Python
  - `command {String}`: attribute where the Python tasks to execute are indicated.

- ## Job npm
  - `command {String}`: attribute where the npm commands you want to execute are indicated. Important, do not put the npm. npm is the default package handler for Node.js, an execution environment for JavaScript.

- ## Job RDM Approval
  This job does not have any of its own configuration attributes, only those inherited from Job.

- ## Job Rundeck
  - `command {Map}`: An attribute that indicates the parameters needed to run Rundeck jobs.
  - `jobIdentifier {String}`:  identifier of the rundeck job you want to run.
  - `jobOptions {List}`: list of parameters that we want to pass to the rundeck job. Parameters have the structure name = value.
  - `tagDeployment {Boolean}`: if set to true, add a git tag with build version value after deploying rpm to LIVE.

- ## Job Nexus
  - `nexusRepositoryId {String}`: attribute where the Nexus repository id is indicated. The repository id has the following format:  `{environmentId}-{businessId}-{providerId}-{regionId}-{solutionAlias}-repo`.
    - **`environmentId`**: `dev`, `test`, `stage`, `live`, `int`.
    - **`providerId`**: `hbg`, `bt`, `aws`.
    - **`businessId`**: `hbg`, `dps`.
    - **`regionId`**: `palma`, `madrid`, `eu-west-1`, `us-east-1`, `ap-southeast-1`.
  - `environment {String}`: name of the environment in which the Nexus repository is located. Possible values:
    - **shared-{businessId}-{providerId}-{regionId}**:  all binaries for all environments.
  The new Nexus url is as follows:: `https://nexus-repository.${Environment.SHARED.code}-${business.code}-${provider.code}-${region.code}.hotelbeds.com/nexus/content/repositories`

- ## Job ace
  - `command {String}`: attribute that indicates the job execution mode. Possible values: build to compile and package to generate rpm. Possible values:
    - **build**: to compile.
    - **package** to generate rpm.

- ## Job Docker
  For this type of job, jenkinsAgentTag value will be overridden so you don't need to specify any value.
  - `image {String}`: An attribute that indicates the docker image needed to run the job. Available values:
    - **`SERVERLESS`**: to run Serverless commands.
    - **`ARGOCD`**:  to run Argo CD deployments in a Kubernetes cluster.
  - `parameters {List}`: list of parameters that we want to pass to the job. Format of parameters according to the executable you are using.
  - `awsProfile {string}`: Only when image value is `SERVERLESS`. Format {environmentId}-{businessId}-{providerId}-{regionId}


- ## Job Template
  This type of job is to indicate a template to be used to generate one or more jobs, depending on the template. Its purpose is to simplify the configuration of a pipeline.
  
  **How to use**:
  
  For this type of job, only use this attributes (others are ignored):
  
  - `type {String}`: must be template
  - `profiles{List}`: as described in Job section. All created jobs from this template will have this profiles.
  - `template {String}`: the name of the template to use. See available templates below.
  - `parameters {List}`: list of parameters required for this template. Format key: value
  
  Example:
  ```
         - type: template
            profiles: ON_PREMISE
            template: ARGOCD
            parameters:
              environment: test
  ```
  
  Available templates:
  - **CI**: Create continuous integration jobs for java projects recommended by the delivery engineering team.
    - `required parameters`:
      - `buildCommand {String}`: Command for build job. Example `clean compile -X -e -U`
      - `utCommand {String}`: Command for unit tests job. Example `clean test -X -e`
      - `itCommand {String}`: Command for integration tests job. Example `clean integration-test -Dtest=ignoreUnitTests -Dspring.profiles.active=test,ci`
      - `analysisCommand {String}`: Command for code analysis job. Example `sonar:sonar -Dsonar.analysis.mode=publish -Dsonar.buildbreaker.skip=false`
  - **PACKAGE_DOCKER**: Create a job that generates a docker image from the Dockerfile of the project and pushes it to the ECR registry in AWS
  - **ARGOCD**: Create the necessary jobs to perform a deployment with ArgoCD in a Kubernetes cluster
   - `required parameters`:
        - `environment {String}`: name of the environment where you want to deploy your application
       
# Meta-information file {service_name}.yml

File that is stored in the Bitbucket jenkins-pipelines repository. In this file you will find:

- `jenkinsPath* {String}`: pipeline path, within the Jenkins folder ecosystem.
- `scmRepository {String}`: default repository that all the jobs in the pipeline will throw.
- `scmBranch {String}`: default repository that all the jobs in the pipeline will throw.
- `technology {String}`:  technology on which the pipeline is based.
List of possible values:
  - **Spring Boot**: for services based on Spring Boot and created from the archetype.
  - **AngularJS**:  for front.
  - **Java Function**:  for AWS lambda functions in Java.
  - **Python Function**: for AWS lambda functions in Python
  - **Python**: for rpm Python services.
  - **ACE**: for compilations of ACE Daemon (Only available in `releasementStrategy`: RELEASES).
- `releasementStrategy`: Indicates the mode in which the pipeline works. List of possible values:
  - **CONTINUOUS_DEPLOYMENT**: Default mode.
  - **RELEASES**: a pipeline is generated per environment, using different branches of the repository. DEV (develop branch), TEST (dynamic branch, always release type) and STAGE (master branch).
- `builtByVersion {String}`: Indicates the version of the pipelines scripts that have generated or regenerated the pipeline. This value is taken from the pipelines.metadata file of the pipelines scripts. It must be modified every time changes are made to the scripts.

[jobName]: doc-resources/img/jobname.jpg
[jobShowName]: doc-resources/img/jobshowname.jpg
[flow]: doc-resources/img/flow.jpg
