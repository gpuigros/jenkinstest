//@Autoapproved


//import com.hotelbeds.jenkins.pipelines.Profile
import jenkins.util.*;
import jenkins.model.*;


def thr = Thread.currentThread()
def currentBuild = thr?.executable
def buildEnvVarsMap = currentBuild.envVars

def map = [:]

String repo = buildEnvVarsMap?.GIT_REPO_URL
println "repo: $repo"
String projectName = repo.replaceFirst(~/ssh:\/\/git@stash.hotelbeds[:\d]+\/[\w\d_-]*\//, '')
println "projectName: $projectName"
projectName = projectName.replaceFirst(~/\.git/, '')
map << [PROJECT_NAME: projectName]


//Profile.values().each { profile ->
//    String profileTagKey= profile.toString() + "_TAGS"
//    def profileTag = profile.code.toUpperCase()
//    map << [ "${profileTagKey}" : "${profileTag}"]
//}

map << [DPS_TAGS: 'DPS']
map << [AWS_TAGS: 'AWS']
map << [ON_PREMISE_TAGS: '']

return map
