package com.getstructure.crm

import com.getstructure.GSAbstractController
import com.getstructure.crm.model.HubspotIntakePanelRequest
import com.getstructure.crm.model.HubspotOauthRedirectRequest
import com.getstructure.crm.model.TypeFormOnboardRequest
import grails.converters.JSON
import org.springframework.http.HttpStatus

class CrmController extends GSAbstractController {

    CrmService crmService

    def hubspotRedirectHandler(HubspotOauthRedirectRequest request) {
        render crmService.handleHubspotRedirect(request) as JSON
    }

    def hubspotIntakePanel(HubspotIntakePanelRequest request) {
        render crmService.getIntakePanel(request) as JSON
    }

    def hubspotLoadCustomerLead(HubspotIntakePanelRequest intakePanelRequest) {
        render crmService.hubspotLoadCustomerLead(intakePanelRequest) as JSON
    }

    def typeFormOnboardingHandler(TypeFormOnboardRequest typeFormOnboardRequest) {
        crmService.typeFormOnboardingHandler(typeFormOnboardRequest)
        render HttpStatus.OK
    }

}
