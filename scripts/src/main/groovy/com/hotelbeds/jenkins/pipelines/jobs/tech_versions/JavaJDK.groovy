package com.hotelbeds.jenkins.pipelines.jobs.tech_versions

enum JavaJDK {

    JDK_18_U45('JDK_1.8_u45'),
    JDK_18_U161('JDK_1.8_u161'),
    JDK_18_U192('JDK_1.8_u192'),
    JDK_11_0_2u9('OpenJDK_11.0.2u9')

    def code

    private JavaJDK(Object code) {
        this.code = code
    }

}
