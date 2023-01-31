package com.getstructure.mergeApi.model

import grails.validation.Validateable

class MergeApiSaveAccountTokenRequest implements Validateable{
    String publicToken
    String customerSoftwareId
    String getStructureUserId
    String integrationType

    static constraints = {
        publicToken nullable: false
        customerSoftwareId nullable: false
        getStructureUserId nullable: false
        integrationType nullable: false
    }
}
