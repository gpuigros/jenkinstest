package com.hotelbeds.jenkins.pipelines

enum Technology {

    SPRING_BOOT('Spring Boot'),
    ANGULAR_JS('AngularJS'),
    JAVA_FUNCTION('Java Function'),
    PYTHON_FUNCTION('Python Function'),
    PYTHON('Python'),
    ACE('ACE')

    def code

    private Technology(Object code) {
        this.code = code
    }

}
