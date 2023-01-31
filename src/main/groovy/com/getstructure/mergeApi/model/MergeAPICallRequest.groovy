package com.getstructure.mergeApi.model

import grails.validation.Validateable

class MergeAPICallRequest implements Validateable {
    String customerId
    String softwareName
    String apiActionKey
    String remoteUserEmail
    String remoteTicketId
    Map<List,String> params
}
