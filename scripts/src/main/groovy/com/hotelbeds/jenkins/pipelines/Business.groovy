package com.hotelbeds.jenkins.pipelines


enum Business {

    HBG('hbg'),
    DPS('dps')


    def code

    private Business(Object code) {
        this.code = code
    }

}
