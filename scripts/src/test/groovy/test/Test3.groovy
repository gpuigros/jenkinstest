import org.yaml.snakeyaml.Yaml

import com.hotelbeds.jenkins.Constants
import com.hotelbeds.jenkins.pipelines.Pipeline
import com.hotelbeds.jenkins.pipelines.jobs.RundeckJob
import com.hotelbeds.jenkins.pipelines.stages.Stage
import com.hotelbeds.jenkins.pipelines.jobs.Job
import com.hotelbeds.jenkins.pipelines.jobs.MavenJob
import com.hotelbeds.jenkins.pipelines.jobs.NexusJob


def profiles = ['AWS', 'ON_PREMISE', 'DPS', 'AWS_US_WEST_1', 'AWS_AP_SOUTHEAST_1']

def final SPRING_BOOT_TEMPLATE = '''
OLAKEASE
  - UPLOAD_ARTIFACT_DEV_${profiles.join("_REPO:\\n    - \'hello-world-service-rpm/target/rpm/hello-world-service/RPMS/noarch/hello-world-service-\\$#@#RPM_VERSION#~#-\\$#@#RPM_RELEASE#~#.noarch.rpm\'\\n  - UPLOAD_ARTIFACT_DEV_")}_REPO:\\n    - \'hello-world-service-rpm/target/rpm/hello-world-service/RPMS/noarch/hello-world-service-\\${RPM_VERSION}-\\${RPM_RELEASE}.noarch.rpm\'
CANTAOKEASE
'''


def engine = new groovy.text.SimpleTemplateEngine()
def output = engine.createTemplate(SPRING_BOOT_TEMPLATE).make(profiles: profiles).toString()

println output.replaceAll('#@#', '{').replaceAll('#~#', '}')

