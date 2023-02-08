package com.getstructure.crm.model

import grails.validation.Validateable

class HubspotOauthRedirectRequest implements Validateable {
    String code
    String state

    static constraints = {
        code nullable: false
        state nullable: true
    }
}
