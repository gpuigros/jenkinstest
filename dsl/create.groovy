@Grab('org.yaml:snakeyaml:1.17')
import DeleteOldJobs
import JobsFactory
import templates.*
import org.yaml.snakeyaml.Yaml

def out= getBinding().out;
def deleteJobs=new DeleteOldJobs(out)
def jobsFactory=new JobsFactory(out,this)
def basePath="${PARENT_FOLDER}/${PARENT_FOLDER}"


deleteJobs.deleteOld("${PARENT_FOLDER}","${PARENT_FOLDER}_", "REGENERATOR")


println "basePath = ${basePath}"
//def job11=job("${basePath}_job-dsl-test_BUILD_0")
//jobsFactory.createJob(job11)


jobsFactory.createJob(
        "${basePath}_job-dsl-test_BUILD",
        "BUILD",
        "git://github.com/gpuigros/jenkinstest.git",
        "${basePath}_job-dsl-test_TEST")

jobsFactory.createJob(
        "${basePath}_job-dsl-test_TEST",
        "BUILD",
        "git://github.com/gpuigros/jenkinstest.git",
        "${basePath}_job-dsl-test_TAG")

jobsFactory.createJob(
        "${basePath}_job-dsl-test_TAG",
        "TEST",
        "git://github.com/gpuigros/jenkinstest.git",
        "${basePath}_job-dsl-test_DEPLOY")

jobsFactory.createJob(
        "${basePath}_job-dsl-test_DEPLOY",
        "TEST",
        "git://github.com/gpuigros/jenkinstest.git",
        "")