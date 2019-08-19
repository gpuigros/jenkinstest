export PATH=$PATH:/var/lib/jenkins/tools/hudson.plugins.groovy.GroovyInstallation/Groovy_2.4.5/bin
groovyc -cp ${WORKSPACE}/scripts/lib/snakeyaml-1.17.jar -d ./build ./##PIPELINE_BUILD_SCRIPTS_PATH##*.groovy
cd build && jar cvf ../jenkins-pipelines-scripts.jar .