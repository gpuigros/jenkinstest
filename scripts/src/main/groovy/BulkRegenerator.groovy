import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.pipelines.jobs.HotfixBuilderJob
import com.hotelbeds.jenkins.pipelines.regenerator.RegeneratorJobFactory
import com.hotelbeds.jenkins.utils.Log
import jenkins.model.*
import hudson.model.*
import groovy.io.FileType
import org.yaml.snakeyaml.Yaml

def list = []

def dir = new File("${WORKSPACE}/${Constants.DEFAULT_PIPELINES_DIR}")
dir.eachFileRecurse(FileType.FILES) { file ->
    list << file
}

list.each {
    if (it.name.endsWith('.yml')) {
        println "Processing file " + it.name
        Yaml yaml = new Yaml()
        def pipelineYmlFileName = it.name

        Log.configure(new File("${WORKSPACE}/logs/pipeline.log"))

        def pipelineMetadata = yaml.load(readFileFromWorkspace("${Constants.DEFAULT_PIPELINES_DIR}/${pipelineYmlFileName}"))
        String pipelineName = "${pipelineYmlFileName}"

        def pipeline = new Pipeline(pipelineMetadata)
        pipeline.name = (pipelineName.contains(Constants.BRANCH_SEPARATOR) ? pipelineName.split(Constants.BRANCH_SEPARATOR).first() : pipelineName).replaceAll('.yml', '')
        def regeneratorJob = RegeneratorJobFactory.getInstance(pipeline, pipelineName)
        regeneratorJob.build(this)

        if (pipeline.releasementStrategy == Constants.CONTINUOUS_DEPLOYMENT_RELEASEMENT_STRATEGY) {
            def hotfixBuilderJob = new HotfixBuilderJob(pipeline)
            hotfixBuilderJob.build(this)
        }

    }
}


