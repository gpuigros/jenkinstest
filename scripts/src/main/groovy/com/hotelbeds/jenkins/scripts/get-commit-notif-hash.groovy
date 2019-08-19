//@Autoapproved

import hudson.model.*

def result = manager.getLogMatcher("commit notification .{40}").group(0).replaceAll("commit notification ", "")
manager.addShortText(result)

def pa = new ParametersAction([
    new StringParameterValue("GIT_NOTIF_COMMIT", result)
])

// add variable to current job
Thread.currentThread().executable.addAction(pa)
