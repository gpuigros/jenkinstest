package com.hotelbeds.jenkins.pipelines


enum Region {

    PALMA('palma'),
    MADRID('madrid'),
    EU_WEST_1('eu-west-1'),
    US_EAST_1('us-east-1'),
    AP_SOUTHEAST_1('ap-southeast-1')


    def code

    private Region(Object code) {
        this.code = code
    }

}
