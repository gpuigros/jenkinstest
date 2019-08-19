import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Technology
import com.hotelbeds.jenkins.pipelines.templates.PipelineTemplateBuilder
import com.hotelbeds.jenkins.pipelines.templates.angularjs.AngularJSTemplate
import com.hotelbeds.jenkins.pipelines.templates.functions.JavaFunctionTemplate
import com.hotelbeds.jenkins.pipelines.templates.functions.PythonFunctionTemplate
import com.hotelbeds.jenkins.pipelines.templates.metadata.MetadataBuilder
import com.hotelbeds.jenkins.pipelines.templates.metadata.MetadataTemplate
import com.hotelbeds.jenkins.pipelines.templates.python.PythonTemplate
import com.hotelbeds.jenkins.pipelines.templates.releases.ReleaseTemplate
import com.hotelbeds.jenkins.pipelines.templates.releases.ace.ACETemplate
import com.hotelbeds.jenkins.pipelines.templates.springboot.SpringBootTemplate

def final RPM_PACKAGE = 'rpm'
def final ZIP_PACKAGE = 'zip'

def profiles = PROFILES.replaceAll(' ', '').split('\\|\\|')

def releasementStrategy = "${RELEASEMENT_STRATEGY}"

def tags
if (PROFILES.contains('ON_PREMISE')) {
    tags = 'ON_PREMISE'
} else {
    tags = PROFILES
}

if (releasementStrategy == Constants.CONTINUOUS_DEPLOYMENT_RELEASEMENT_STRATEGY) {

    def metadataTemplate = MetadataBuilder
            .builder()
            .jenkinsPath("${FOLDER}")
            .pipelineName("${projectName}")
            .scmRepository("${GIT_REPO_URL}")
            .scmBranch("${GIT_BRANCH}")
            .technology("${TECHNOLOGY}")
            .workspace("${workspace}")
            .releasementStrategy("${releasementStrategy}")
            .template(new MetadataTemplate())
            .build()

    def metadataTemplateFile = metadataTemplate.toFile()
    println metadataTemplateFile.getText()

    def pipelineTemplate
    if (Technology.SPRING_BOOT.code == TECHNOLOGY) {
        pipelineTemplate = PipelineTemplateBuilder
                .builder()
                .name("${projectName}")
                .profiles(profiles)
                .scmRepository("${GIT_REPO_URL}")
                .tags(tags)
                .template(new SpringBootTemplate([profiles: profiles]))
                .workspace("${workspace}")
                .solutionAlias("${SOLUTION_ALIAS}")
                .packageType(RPM_PACKAGE)
                .build()
    } else if (Technology.ANGULAR_JS.code == TECHNOLOGY) {
        pipelineTemplate = PipelineTemplateBuilder
                .builder()
                .name("${projectName}")
                .profiles(profiles)
                .scmRepository("${GIT_REPO_URL}")
                .tags(tags)
                .template(new AngularJSTemplate([profiles: profiles]))
                .workspace("${workspace}")
                .solutionAlias("${SOLUTION_ALIAS}")
                .packageType(ZIP_PACKAGE)
                .build()
    } else if (Technology.JAVA_FUNCTION.code == TECHNOLOGY) {
        pipelineTemplate = PipelineTemplateBuilder
                .builder()
                .name("${projectName}")
                .profiles(profiles)
                .scmRepository("${GIT_REPO_URL}")
                .tags(tags)
                .template(new JavaFunctionTemplate([profiles: profiles]))
                .workspace("${workspace}")
                .solutionAlias("${SOLUTION_ALIAS}")
                .packageType(ZIP_PACKAGE)
                .build()
    } else if (Technology.PYTHON_FUNCTION.code == TECHNOLOGY) {
        pipelineTemplate = PipelineTemplateBuilder
                .builder()
                .name("${projectName}")
                .profiles(profiles)
                .scmRepository("${GIT_REPO_URL}")
                .tags(tags)
                .template(new PythonFunctionTemplate([profiles: profiles]))
                .workspace("${workspace}")
                .solutionAlias("${SOLUTION_ALIAS}")
                .packageType(ZIP_PACKAGE)
                .build()
    } else if (Technology.PYTHON.code == TECHNOLOGY) {
        pipelineTemplate = PipelineTemplateBuilder
                .builder()
                .name("${projectName}")
                .profiles(profiles)
                .scmRepository("${GIT_REPO_URL}")
                .tags(tags)
                .template(new PythonTemplate([profiles: profiles]))
                .workspace("${workspace}")
                .solutionAlias("${SOLUTION_ALIAS}")
                .packageType(RPM_PACKAGE)
                .build()
    }

    def pipelineTemplateFile = pipelineTemplate.toFile()
    println pipelineTemplateFile.getText()

} else if (releasementStrategy == Constants.RELEASES_RELEASEMENT_STRATEGY) {
    File environmentsContainer = new File("${workspace}/environments")
    environmentsContainer.mkdirs()

    def createEnvironment = { environmentName, branch ->
        File environmentContainer = new File(environmentsContainer, environmentName)
        environmentContainer.mkdirs()

        def metadataTemplate = MetadataBuilder
                .builder()
                .jenkinsPath("${FOLDER}/${environmentName}")
                .pipelineName("${projectName}")
                .scmRepository("${GIT_REPO_URL}")
                .scmBranch("${branch}")
                .technology("${TECHNOLOGY}")
                .workspace("${workspace}")
                .releasementStrategy("${releasementStrategy}")
                .template(new MetadataTemplate())
                .build()

        def metadataTemplateFile = metadataTemplate.toFile()
        println metadataTemplateFile.getText()

        def pipelineTemplate
        if (Technology.ACE.code == TECHNOLOGY) {
            pipelineTemplate = PipelineTemplateBuilder
                    .builder()
                    .name("${projectName}")
                    .profiles(profiles)
                    .scmRepository("${GIT_REPO_URL}")
                    .tags(tags)
                    .template(new ACETemplate([profiles: profiles, environment: environmentName]))
                    .workspace("${workspace}")
                    .solutionAlias("${SOLUTION_ALIAS}")
                    .packageType(RPM_PACKAGE)
                    .environment(environmentName)
                    .build()
        } else {
            pipelineTemplate = PipelineTemplateBuilder
                    .builder()
                    .name("${projectName}")
                    .profiles(profiles)
                    .scmRepository("${GIT_REPO_URL}")
                    .tags(tags)
                    .template(new ReleaseTemplate([profiles: profiles, environment: environmentName]))
                    .workspace("${workspace}")
                    .solutionAlias("${SOLUTION_ALIAS}")
                    .packageType(RPM_PACKAGE)
                    .environment(environmentName)
                    .build()
        }


        def pipelineTemplateFile = pipelineTemplate.toFile(environmentContainer, environmentName)
        println pipelineTemplateFile.getText()
    }

    createEnvironment('DEV', "${DEV_BRANCH}")
    createEnvironment('TEST', '$RELEASE_BRANCH')
//	createEnvironment('LIVE', '$RELEASE_TAG')
//	
    if (PROFILES.contains('ON_PREMISE')) {
        createEnvironment('STAGE', '$HOTFIX_BRANCH')
    }
}

