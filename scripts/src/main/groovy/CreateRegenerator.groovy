import com.hotelbeds.jenkins.pipelines.regenerator.RegeneratorJobFactory
import jenkins.model.*

import org.yaml.snakeyaml.Yaml

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.utils.Log

Yaml yaml = new Yaml()

Log.configure(new File("${WORKSPACE}/logs/pipeline.log"))

def pipelineMetadata = yaml.load(readFileFromWorkspace("${Constants.DEFAULT_PIPELINES_DIR}/${PIPELINE}"))
String pipelineName = "${PIPELINE}"

def pipeline = new Pipeline(pipelineMetadata)
pipeline.name = (pipelineName.contains(Constants.BRANCH_SEPARATOR) ? pipelineName.split(Constants.BRANCH_SEPARATOR).first() : pipelineName).replaceAll('.yml', '')
def regeneratorJob = RegeneratorJobFactory.getInstance(pipeline, pipelineName)
regeneratorJob.build(this)
