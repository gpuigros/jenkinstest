import com.cloudbees.hudson.plugins.folder.Folder

class DeleteOldJobs  {

static def deleteOld(String folderName) {

        def allJobs= hudson.model.Hudson.getInstance().getItems()

        for(int i=0; i<allJobs.size(); i++){
            def job = allJobs[i]
            if(job instanceof hudson.model.Project && job .getLastBuild() != null ){
                processJob(job)
            }else if(job instanceof Folder){
                processFolderByName(job)
            }
        }

        void processFolderByName(Item folder){
        if(folder.getFullName().contains(folderName))
        processFolder(folder)
        }

        void processFolder(Item folder){
            //println "Processing Folder -"+folder.getFullName()
            folder.getItems().each{
                if(it instanceof com.cloudbees.hudson.plugins.folder.AbstractFolder){
                    processFolder(it)
                }else{
                    processJob(it)
                }
            }
        }

        void processJob(Item job){
            println  job.getFullName()
        // you can do operations like enable to disable
        // job.disable()
        }
    }


}
