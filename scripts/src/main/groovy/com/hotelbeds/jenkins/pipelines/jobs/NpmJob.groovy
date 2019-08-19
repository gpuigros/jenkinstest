package com.hotelbeds.jenkins.pipelines.jobs

import com.hotelbeds.jenkins.pipelines.jobs.tech_versions.NodeJS

import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureDefaultDatadogTags
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureImportedArtifacts
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureInjectGlobalPasswords
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureNodeJS
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureUpdateBuildName
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.configureEnvInjectBuilder
import static com.hotelbeds.jenkins.pipelines.helper.ConfigureHelper.getScript

import com.hotelbeds.jenkins.Constants


class NpmJob extends Job {

    def command

    def nodejsVersion


    NpmJob(Object config) {
        super(config)
    }


    @Override
    def build(dslFactory) {

        if (!isValid()) {
            throw new Error("${this.name} has validation errors")
        }

        def updateNpmVersionPropertiesScript = getScript(dslFactory, 'update-npm-version-properties.sh')
        updateNpmVersionPropertiesScript = updateNpmVersionPropertiesScript.replaceAll("##DEFAULT_PROJECT_DIR##", Constants.DEFAULT_PROJECT_DIR)

        def dslJob = dslFactory.job(getJenkinsJobName())
        configureDefaultDatadogTags(this)
        dslJob = super.init(dslJob)
        dslJob.configure configureImportedArtifacts(this)
        dslJob.configure configureNodeJS()
        dslJob.configure configureInjectGlobalPasswords()

        if (this.stage.pipeline.nodejsVersion == NodeJS.NodeJS_10_4_0.code) {
            nodejsVersion = NodeJS.NodeJS_10_4_0.code
        } else {
            //Default version
            nodejsVersion = NodeJS.NodeJS_8_9_4.code
        }

        dslJob.wrappers {
            nodejs(nodejsVersion)
        }

        if (this.firstJobOfPipeline) {
            dslJob.steps {
                shell('''cd ''' + Constants.DEFAULT_PROJECT_DIR + '''
rm -f version.properties
VERSION=`cat package.json | grep version | head -1 | awk -F: '{ print $2 }' | sed 's/[",]//g' | tr -d '[[:space:]]'`
echo "$VERSION" >> version.properties
	'''.stripMargin())
                shell(updateNpmVersionPropertiesScript)
            }
            dslJob.configure configureUpdateBuildName(this)
            dslJob.configure configureEnvInjectBuilder(this)
        }


        dslJob.steps {
            shell('''
cd ''' + Constants.DEFAULT_PROJECT_DIR + '''
echo '
_auth=${HBEDS_NEXUS_NPM_REPO_AUTH}
email=devenvironments@hotelbes.com
' >> .npmrc''')
            shell ('''
cd ''' + Constants.DEFAULT_PROJECT_DIR + '''
npm install''')
            shell('''
cd ''' + Constants.DEFAULT_PROJECT_DIR + '''
npm ''' + "$command")
        }


        // Remove the disabled message
        dslJob.configure { project ->
            project.remove(project / 'postbuilders')
        }
        super.finalize(dslJob)
    }
}
