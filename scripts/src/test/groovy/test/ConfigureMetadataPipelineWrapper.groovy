import com.hotelbeds.jenkins.Constants

def workspace = new File("D:/Workspaces/envtools-workspace/jenkins-pipelines-scripts")
def script = new File(workspace, "src/main/groovy/ConfigurePipeline.groovy")

this.class.classLoader.addURL(script.toURI().toURL())

def bindings = new Binding()
bindings.setVariable("PROFILES", "ON_PREMISE||AWS||AWS_US_WEST_1||AWS_AP_SOUTHEAST_1")
bindings.setVariable("FOLDER", "ENV AND TOOLS")
bindings.setVariable("GIT_REPO_URL", "ssh://git@stash.hotelbeds:7999/envtools/hello-world-service.git")
bindings.setVariable("GIT_BRANCH", "feature/superfeature")
bindings.setVariable("TECHNOLOGY", "Spring Boot")
bindings.setVariable("SOLUTION_ALIAS", 'hello-world-solution')
bindings.setVariable("RELEASEMENT_STRATEGY", Constants.RELEASES_RELEASEMENT_STRATEGY)
bindings.setVariable("TAGS", "")
bindings.setVariable("workspace", workspace.absolutePath)
bindings.setVariable("projectName", "hello-world-service")

def shell = new GroovyShell(bindings)
String result = shell.evaluate(script.getText())
