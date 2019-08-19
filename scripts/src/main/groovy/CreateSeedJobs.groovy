import com.hotelbeds.jenkins.pipelines.seedjobs.BulkGeneratorJob
import com.hotelbeds.jenkins.pipelines.seedjobs.ConfigGeneratorJob
import com.hotelbeds.jenkins.pipelines.seedjobs.GeneratorJob
import com.hotelbeds.jenkins.pipelines.seedjobs.releases_mode.ReleasesBulkGeneratorJob
import com.hotelbeds.jenkins.pipelines.seedjobs.releases_mode.ReleasesConfigGeneratorJob
import com.hotelbeds.jenkins.pipelines.seedjobs.releases_mode.ReleasesGeneratorJob
import com.hotelbeds.jenkins.utils.Log

Log.configure(new File("${WORKSPACE}/logs/pipeline.log"))

def mode = "${MODE}"
def configGeneratorJob
def generatorJob
def bulkGeneratorJob

if (mode == "NORMAL") {
    configGeneratorJob = new ConfigGeneratorJob("${DESTINATION_PATH}")
    generatorJob = new GeneratorJob("${DESTINATION_PATH}")
    bulkGeneratorJob = new BulkGeneratorJob("${DESTINATION_PATH}")
} else if (mode == "RELEASES_COMPATIBILITY") {
    configGeneratorJob = new ReleasesConfigGeneratorJob("${DESTINATION_PATH}")
    generatorJob = new ReleasesGeneratorJob("${DESTINATION_PATH}")
    bulkGeneratorJob = new ReleasesBulkGeneratorJob("${DESTINATION_PATH}")
}

configGeneratorJob.build(this)
generatorJob.build(this)
bulkGeneratorJob.build(this)
