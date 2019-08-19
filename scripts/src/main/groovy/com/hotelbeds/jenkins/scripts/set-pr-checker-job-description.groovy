//@Autoapproved
def currentBuild = Thread.currentThread().executable
def PULL_REQUEST_URL = build.buildVariableResolver.resolve('PULL_REQUEST_URL')
def PULL_REQUEST_ID = build.buildVariableResolver.resolve('PULL_REQUEST_ID')
def description = "<a href='$PULL_REQUEST_URL'>PR #$PULL_REQUEST_ID</a>"
currentBuild.setDescription(description)