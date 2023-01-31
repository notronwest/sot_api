package com.getstructure.mergeApi.model

import grails.validation.Validateable

class MergeApiStartRequest implements Validateable{
    String customerSoftwareId
    String getStructureUserId
    String integrationType

    static constraints = {
        customerSoftwareId nullable: false
        getStructureUserId nullable: false
        integrationType nullable: false
    }
}
