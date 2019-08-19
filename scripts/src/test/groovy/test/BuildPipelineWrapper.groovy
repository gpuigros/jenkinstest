def workspace = new File("D:/Workspaces/envtools-workspace/jenkins-pipelines-scripts")
def script = new File(workspace, "src/main/groovy/BuildPipeline.groovy")

this.class.classLoader.addURL(script.toURI().toURL())

def bindings = new Binding()
bindings.setVariable("workspace", workspace.absolutePath)
bindings.setVariable("projectName", "test-app")

def shell = new GroovyShell(bindings)
String result = shell.evaluate(script.getText())
