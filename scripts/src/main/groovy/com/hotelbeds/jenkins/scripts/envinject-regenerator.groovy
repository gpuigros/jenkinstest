//@Autoapproved

@Grab('org.yaml:snakeyaml:1.17')

import jenkins.model.*
import hudson.FilePath
import jenkins.model.*
import com.cloudbees.hudson.plugins.folder.*
import org.yaml.snakeyaml.Yaml

def pipelineView = Jenkins.instance.getItemByFullName('##PIPELINE_JENKINS_PATH##')
def firstJobOfPipelineName = pipelineView?.views?.items?.first()?.first()?.name

def map = [:]
def nextBuildNumber = 1
if (firstJobOfPipelineName) {
    def job = Jenkins.instance.getItemByFullName('##PIPELINE_JENKINS_PATH##/' + firstJobOfPipelineName)
    if (job != null) {
        nextBuildNumber = job.nextBuildNumber
    }
}
map.put('NEXT_BUILD_NUMBER', nextBuildNumber)

Yaml parser = new Yaml()
def channel = currentBuild.workspace.channel
def filePath = new FilePath(channel, "${currentBuild.workspace}/##DEFAULT_PROJECT_DIR##/##YML_FILE##")
def configuration = parser.load(filePath.readToString())
map.put('EMAIL_RECIPIENTS', configuration?.pipeline?.recipients == null ? '' : configuration?.pipeline?.recipients)

return map
