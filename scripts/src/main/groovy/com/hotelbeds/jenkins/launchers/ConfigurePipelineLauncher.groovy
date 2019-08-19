import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.CompilerConfiguration
import jenkins.util.*
import jenkins.model.*

def thr = Thread.currentThread()
def currentBuild = thr?.executable
def buildEnvVarsMap = currentBuild.envVars
println buildEnvVarsMap

def workspace = new File(buildEnvVarsMap?.WORKSPACE)
def scriptsJar = new File(workspace, 'jenkins-pipelines-scripts.jar')
def script = new File(workspace, 'scripts/src/main/groovy/ConfigurePipeline.groovy')

// Add imports for script.
def importCustomizer = new ImportCustomizer()

// Configure compiler
def compilerConfiguration = new CompilerConfiguration()
compilerConfiguration.addCompilationCustomizers(importCustomizer)
compilerConfiguration.setClasspathList([scriptsJar.absolutePath] as List)

// Parameter bindings
def bindings = new Binding()

bindings.setVariable("workspace", workspace.absolutePath)
bindings.setVariable("projectName", buildEnvVarsMap?.PROJECT_NAME)

bindings.setVariable("PROFILES", buildEnvVarsMap?.PROFILES)
def folderPath = "${buildEnvVarsMap?.FOLDER}".replaceAll(' >> ', '/').replaceAll(' ', '-')
bindings.setVariable("FOLDER", folderPath)
bindings.setVariable("GIT_REPO_URL", buildEnvVarsMap?.GIT_REPO_URL)
bindings.setVariable("TECHNOLOGY", buildEnvVarsMap?.TECHNOLOGY)
def tagKey = "${buildEnvVarsMap?.PROFILES}_TAGS".toString()
bindings.setVariable("TAGS", buildEnvVarsMap.get(tagKey))
bindings.setVariable("SOLUTION_ALIAS", buildEnvVarsMap?.SOLUTION_ALIAS)
bindings.setVariable("RELEASEMENT_STRATEGY", buildEnvVarsMap?.RELEASEMENT_STRATEGY)

// Branch for Continuous Deployment
bindings.setVariable("GIT_BRANCH", buildEnvVarsMap?.GIT_BRANCH)

// Branches for releases
bindings.setVariable("DEV_BRANCH", buildEnvVarsMap?.DEV_BRANCH)
bindings.setVariable("TEST_BRANCH", buildEnvVarsMap?.TEST_BRANCH)
bindings.setVariable("STAGE_BRANCH", buildEnvVarsMap?.STAGE_BRANCH)

// Create shell and execute script.
def shell = new GroovyShell(bindings, compilerConfiguration)
shell.evaluate(script.getText())
