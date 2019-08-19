package com.hotelbeds.jenkins.pipelines


enum Profile {

    ON_PREMISE('', Business.HBG, Provider.BT, Region.MADRID),
    DPS('dps', Business.DPS, Provider.AWS, Region.EU_WEST_1),
    AWS('aws', Business.HBG, Provider.AWS, Region.EU_WEST_1),
    AWS_US_EAST_1('awsuseast1', Business.HBG, Provider.AWS, Region.US_EAST_1),
    AWS_AP_SOUTHEAST_1('apsoutheast1', Business.HBG, Provider.AWS, Region.AP_SOUTHEAST_1)

    //Code is used as tag for datadog
    def code
    Business business
    Provider provider
    Region region

    private Profile(Object code, Business business, Provider provider, Region region) {
        this.code = code
        this.business = business
        this.provider = provider
        this.region = region
    }

    static final String SEPARATOR = "-"
    static final String RUNDECK_PARAMETER_SEPARATOR = "_"

    String getNexusDefinition() {
        return this.business.code + SEPARATOR + this.provider.code + SEPARATOR + this.region.code
    }

    String getRundeckEnvironmentDefinition(String environmentId) {
        def environmentPrefix = this.business.code + RUNDECK_PARAMETER_SEPARATOR + this.provider.code + RUNDECK_PARAMETER_SEPARATOR + this.region.code
        return environmentPrefix.toUpperCase() + ' ' + environmentId
    }

}
