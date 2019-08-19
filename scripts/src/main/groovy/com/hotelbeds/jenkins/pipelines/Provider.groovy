package com.hotelbeds.jenkins.pipelines


enum Provider {

    HBG('hbg'),
    BT('bt'),
    AWS('aws')


    def code

    private Provider(Object code) {
        this.code = code
    }

}
