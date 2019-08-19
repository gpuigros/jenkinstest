//@Autoapproved

import jenkins.util.*
import jenkins.model.*
import java.nio.file.Files
import java.nio.file.Paths

String jenkinsHome = Jenkins.instance.getRootDir().absolutePath

def folders = []
def list
list = { String parent ->
    new File(parent, "jobs").eachDir() { dir ->
        def dirPath = dir.getPath()
        def configFile = new File(dir, "config.xml")
        if (configFile.exists()) {
            if (configFile.text.contains('com.cloudbees.hudson.plugins.folder.Folder')) {
                def folder = dirPath.replaceAll('/var/lib/jenkins/', '').replaceAll('jobs/', '')
                folders << folder
                if (Files.exists(Paths.get("$dirPath/jobs"))) {
                    list(dirPath)
                }
            }
        }
    }
}

list(jenkinsHome)
folders.sort(false).collect { it.replaceAll('-', ' ').replaceAll('/', ' >> ').toUpperCase() }
