//@Autoapproved


import groovy.json.JsonSlurper

import java.text.SimpleDateFormat


/**
 * This script prepare the environment to regenerate the pipeline.
 *
 * @arg [0]url lastSuccessfulBuild ex: "http://jenkins.service/job/env-and-tools/job/test-app/job/test-app_REGENERATOR/lastSuccessfulBuild/api/json"
 * @arg [1]buildCause ex: $BUILD_CAUSE
 */

def regenerateFile = new File('regenerate.pipeline')
regenerateFile.delete()

def url = args[0]
def buildCause = args[1]

def final DATE_FORMAT = 'yyyy-MM-dd HH:mm:ssZ'
def git = "git --git-dir ./##DEFAULT_PROJECT_DIR##/.git log -n 1 --date=iso --format=%cd"
def gitYmlFile = "git --git-dir ./##DEFAULT_PROJECT_DIR##/.git log -n 1 --date=iso --format=%cd -- ##YML_FILE##"
def gitScripts = "git --git-dir ./##DEFAULT_SCRIPTS_DIR##/.git log -n 1 --date=iso --format=%cd -- pipelines.metadata"
println git
println gitYmlFile
println gitScripts

def p1 = git.execute()
p1.waitForOrKill(1000)
def output1 = p1.text
SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT)
formatter.setTimeZone(TimeZone.getTimeZone("GMT"))
def lastCommitDate = formatter.parse(output1)
println "Last commit date for service repository: " + lastCommitDate

def p4 = gitYmlFile.execute()
p4.waitForOrKill(1000)
def output4 = p4.text
def lastCommitDateOnYmlFile = formatter.parse(output4)
println "Last commit date for pipeline.yml: " + lastCommitDateOnYmlFile

def p2 = gitScripts.execute()
p2.waitForOrKill(1000)
def output2 = p2.text
def scriptsLastCommitDate = formatter.parse(output2)
println "Last commit date for jenkins-pipelines-scripts repository: " + scriptsLastCommitDate

def curl = "curl -X GET $url"
println curl
def p3 = curl.execute()
p3.waitForOrKill(1000)
def output3 = p3.text


if (output3.contains('<html>')) {
    regenerateFile.createNewFile()
} else {
    if (buildCause == "MANUALTRIGGER") {
        println "REGENERATOR job started manually"
    }
    def jsonSlurper = new JsonSlurper()
    def lastSuccessfulBuild = jsonSlurper.parseText(output3)
    def lastSuccessfulBuildDate = new Date(lastSuccessfulBuild.timestamp)
    println "Last successful build date: " + lastSuccessfulBuildDate

    //if last commit date is not after last successful build date, job must fail unless manually executed.
    if (buildCause != "MANUALTRIGGER" && !lastCommitDate.after(lastSuccessfulBuildDate)) {
        throw new Error("Last commit date is not after last successful build date. Nothing new to compile")
    } else if (lastCommitDateOnYmlFile.after(lastSuccessfulBuildDate) || scriptsLastCommitDate.after(lastSuccessfulBuildDate)) {
        println "Regenerating pipeline jobs"
        regenerateFile.createNewFile()
    } else {
        println "There are no changes in pipeline.yml and no new jenkins-pipeline-scripts version, so it is not necessary to regenerate the pipeline. Executing job BUILD"
    }
}
