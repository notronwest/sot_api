package com.getstructure.mergedev

import com.getstructure.GSAbstractController
import com.getstructure.mergeApi.model.MergeApiSaveAccountTokenRequest
import com.getstructure.mergeApi.model.MergeApiStartRequest
import com.getstructure.mergeApi.model.MergeAPICallRequest
import grails.converters.JSON

class MergeApiController extends GSAbstractController {

    MergeApiService mergeApiService

    def start(MergeApiStartRequest mergeApiStartRequest) {
        String linkToken = mergeApiService.getMergeLink(mergeApiStartRequest)
        render ( view: '/mergeApiLink', model: [
            linkToken: linkToken,
            getStructureUserId: mergeApiStartRequest.getStructureUserId,
            integrationType: mergeApiStartRequest.integrationType,
            customerSoftwareId: mergeApiStartRequest.customerSoftwareId
        ])
    }

    def saveAccountToken(MergeApiSaveAccountTokenRequest request ) {
        String accountToken = mergeApiService.saveAccountToken(request)
        render accountToken?:'No account token found' as JSON
    }

    def tickets(MergeAPICallRequest request) {
        render mergeApiService.tickets(request) as JSON
    }
}
