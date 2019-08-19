//@Autoapproved

import jenkins.model.*
import jenkins.util.*
import jenkins.model.*

def thr = Thread.currentThread()
def currentBuild = thr?.executable
def buildEnvVarsMap = currentBuild.envVars

def pipeline = Jenkins.instance.getItemByFullName('##PIPELINE_JENKINS_PATH##')
def firstJobOfPipelineName = pipeline?.views?.items?.first()?.first()?.name

def job = Jenkins.instance.getItemByFullName('##PIPELINE_JENKINS_PATH##/' + firstJobOfPipelineName)
job.updateNextBuildNumber(buildEnvVarsMap?.NEXT_BUILD_NUMBER.toInteger())
