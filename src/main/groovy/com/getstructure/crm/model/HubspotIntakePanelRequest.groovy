package com.getstructure.crm.model

import grails.validation.Validateable

class HubspotIntakePanelRequest implements Validateable {
    String userId
    String userEmail
    String associatedObjectId
    String associatedObjectType
    String name
    String address
    String address2
    String city
    String state
    String zip
    String phone
    String domain
    String numberOfEmployees

    static constraints = {
        userId nullable: false
        userEmail nullable: false
        associatedObjectId nullable: false
        associatedObjectType nullable: false
        name nullable: true
        address nullable: true
        address2 nullable: true
        city nullable: true
        state nullable: true
        phone nullable: true
        domain nullable: true
        numberOfEmployees nullable: true
    }
}
