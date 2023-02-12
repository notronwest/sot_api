package com.getstructure.crm.model

import grails.validation.Validateable

class HubspotIntakePanelRequest implements Validateable {
    String hs_object_id
    String dealName
    String userId
    String userEmail
    String associatedObjectId
    String associatedObjectType

    static constraints = {
        hs_object_id nullable: false
        dealName nullable: true
        userId nullable: false
        userEmail nullable: false
        associatedObjectId nullable: false
        associatedObjectType nullable: false
    }
}
