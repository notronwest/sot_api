package com.getstructure.mergedev

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.getstructure.exception.GSException
import com.getstructure.mergeApi.model.MergeAPIParameters
import com.getstructure.mergeApi.model.MergeApiSaveAccountTokenRequest
import com.getstructure.mergeApi.model.MergeApiStartRequest
import com.getstructure.mergeApi.model.MergeAPICallRequest
import com.getstructure.user.model.UserDTO
import com.getstructure.util.GSDateUtil
import grails.gorm.transactions.Transactional
import com.getstructure.GSAbstractService
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import com.getstructure.exception.ErrorCode
import groovy.json.JsonOutput
import groovy.json.JsonBuilder

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Transactional
class MergeApiService extends GSAbstractService {

    static final mergeAPIBaseURL = 'https://api.merge.dev/api/'
    // TODO: move to env variables
    static final mergeAPIKey = 'mJiP_KfPCpftF1gGI0QBRGHl1L56HRDBok4DOJLeSMfJGr-KNsL96g'
    private final RestTemplate restTemplate = new RestTemplate()

    String getMergeLink(MergeApiStartRequest mergeApiStartRequest) {
        // auth

        // operation
        // get some data for this user
        UserDTO user = sql.callWithRows("call getUser_gs(?)",
            [mergeApiStartRequest.getStructureUserId],
            {}).collect {
            new UserDTO(
                userId: it.id,
                fullName: it.full_name,
                firstName: it.first_name,
                lastName: it.last_name,
                email: it.primary_email,
                userType: it.user_type
            )
        }.first()

        Map data = [
            end_user_origin_id: "${mergeApiStartRequest.getStructureUserId}_${mergeApiStartRequest.customerSoftwareId}".toString(), // combine user with customer software to make unique key
            end_user_organization_name: user?user.fullName:'GetStructure Staff',
            end_user_email_address: user?user.email:'api@getstructure.com',
            categories: [ 'hris', 'ats', 'accounting', 'ticketing' ]
        ]
        ObjectMapper mapper = new ObjectMapper()
        String requestBody = mapper.writeValueAsString(data)

        HttpClient client = HttpClient.newHttpClient()
        URI uri = URI.create('https://api.merge.dev/api/integrations/create-link-token')
        HttpRequest request = HttpRequest.newBuilder(uri)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer ${mergeAPIKey}")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody)).build()

        HttpResponse<String> linkTokenResult = client.send(request, HttpResponse.BodyHandlers.ofString())
        String linkToken = new ObjectMapper().readValue(linkTokenResult.body(), ObjectNode.class).get("link_token").textValue()

        return linkToken
    }

    String saveAccountToken(MergeApiSaveAccountTokenRequest request) {
        // auth

        // operation
        HttpClient client = HttpClient.newHttpClient()
        URI uri = URI.create("https://api.merge.dev/api/$request.integrationType/v1/account-token/$request.publicToken")

        HttpRequest tokenRequest = HttpRequest.newBuilder(uri)
            .header("Authorization", "Bearer $mergeAPIKey")
            .build()

        HttpResponse<String> account_token_result = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString())
        String account_token = new ObjectMapper().readValue(account_token_result.body(), ObjectNode.class).get('account_token').textValue()
        if ( account_token ) {
            sql.call('call saveSoftwareOauthToken_gs(?,?,?,?,?)', [
                null,
                request.customerSoftwareId,
                request.getStructureUserId,
                account_token,
                Sql.LONGVARCHAR
            ])
        }
        return account_token
    }

    def tickets(MergeAPICallRequest mergeAPICallRequest) {
        // auth

        // operation
        GroovyRowResult mergeAPIResource = []
        GroovyRowResult accountToken = []
        def data = []
        // get the ticket configuration for the customer
        Map ticketConfig = sql.callWithRows("CALL getCustomerTicketConfig_gs(?)", [ mergeAPICallRequest.customerId ], {}).first()
        // get details about software api
        List mergeAPIResourceList = sql.callWithRows("CALL getMergeAPIResource_gs(?,?)", [mergeAPICallRequest.softwareName, mergeAPICallRequest.apiActionKey], {})

        if (!mergeAPIResourceList || mergeAPIResourceList.isEmpty()) {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Could not get merge api resources for specified request: ${mergeAPICallRequest.softwareName} + ${mergeAPICallRequest.apiActionKey}")
        } else {
            mergeAPIResource = mergeAPIResourceList.first()
        }
        // get account token for customer
        List accountTokenList = sql.callWithRows("CALL getSoftwareOauthTokenForCustomer_gs(?,?)", [mergeAPICallRequest.customerId, mergeAPICallRequest.softwareName], {})

        if (!accountTokenList || accountTokenList.isEmpty()) {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Could not get account token for specified request: customerId: ${mergeAPICallRequest.customerId} + ${mergeAPICallRequest.softwareName}")
        } else {
            accountToken = accountTokenList.first()
        }

        // build API parameters
        MergeAPIParameters params = new MergeAPIParameters(
                accountToken: accountToken.account_token,
                fullAPIURL: "${mergeAPIBaseURL}${mergeAPIResource.baseURL}${mergeAPIResource.url}",
                params: mergeAPICallRequest.params,
                method: mergeAPIResource.method
        )

        // determine what to do with the request
        switch(mergeAPICallRequest.apiActionKey) {
            case 'getTickets':
            case 'syncTickets':
                // using the config make sure we are using the collection_id
                if( ticketConfig ) {
                    params.params.collection_ids = params.params.containsKey('collection_ids' ) ? params.params.collection_ids << ticketConfig.sot_collection_id : [ticketConfig.sot_collection_id]
                }
                data = getData(params)
                // if we are syncing then update the local tickets
                if( mergeAPICallRequest.apiActionKey == 'syncTickets') {
                    // loop through the data and store it
                    data.each { it ->
                        sql.call("{call saveCustomerTicket_gs(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", [
                                null,
                                mergeAPICallRequest.customerId,
                                it.id,
                                it.remote_id,
                                it.parent_ticket,
                                it.name,
                                it.description,
                                it.due_date ? GSDateUtil?.formatDateDBDateTime(it.due_date) : null,
                                it.remote_created_at ? GSDateUtil?.formatDateDBDateTime(it.remote_created_at) : null,
                                it.remote_updated_at ? GSDateUtil?.formatDateDBDateTime(it.remote_updated_at) : null,
                                it.status,
                                it.completed_at ? GSDateUtil?.formatDateDBDateTime(it.completed_at) : null,
                                it.ticekt_url,
                                it.priority,
                                JsonOutput.toJson(it.collections),
                                JsonOutput.toJson(it.assignees),
                                JsonOutput.toJson(it.tags),
                                Sql.LONGVARCHAR
                        ])
                    }
                }
                break
            case 'createTicket':
            case 'updateTicket':
                // get the assignee from the email
                Map assignee = [:]
                List assigneeList = sql.callWithRows("CALL getSoftwareRemoteUserFromEmail_gs(?,?)", [mergeAPICallRequest.softwareName, mergeAPICallRequest.remoteUserEmail], {})
                if( assigneeList && !assigneeList.isEmpty() ) {
                    assignee = assigneeList.first()
                }
                // if there isn't an assignee id and we have no assignee then we gotta exit
                if( !params.params.assignees && !assignee.remote_user_id ) {
                    throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Missing assignee")
                } else if ( !params.params.assignees ) {
                    params.params.assignees = [assignee.remote_user_id]
                }
                // add in the collection_id if it doesn't exist
                if( ticketConfig && !params.params.collections ) {
                    params.params.collections = [ticketConfig.sot_collection_id]
                } else if ( ticketConfig && params.params.collections && !params.params.collections.contains(ticketConfig.sot_collection_id) ) {
                    params.params.collections = params.params.collections << ticketConfig
                }

                // for updating we need to add the remote ticket id to the URL
                if( mergeAPICallRequest.apiActionKey == 'updateTicket' ) {
                    // if we don't have a remote ticket id we gotta exit
                    if( !mergeAPICallRequest.remoteTicketId ) {
                        throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Updates require remote ticket id")
                    }

                    params.fullAPIURL = params.fullAPIURL + '/' + mergeAPICallRequest.remoteTicketId
                }
                // make the call for the API
                data = callAPI(params)

                // store this ticket locally to track
                String customerTicketId = sql.call("{call saveCustomerTicket_gs(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", [
                    null,
                    mergeAPICallRequest.customerId,
                    data.id,
                    data.remote_id,
                    data.parent_ticket,
                    data.name,
                    data.description,
                    data.due_date ? GSDateUtil?.formatDateDBDateTime(data.due_date) : null,
                    data.remote_created_at ? GSDateUtil?.formatDateDBDateTime(data.remote_created_at) : null,
                    data.remote_updated_at ? GSDateUtil?.formatDateDBDateTime(data.remote_updated_at) : null,
                    data.status,
                    data.completed_at ? GSDateUtil?.formatDateDBDateTime(data.completed_at) : null,
                    data.ticekt_url,
                    data.priority,
                    JsonOutput.toJson(data.collections),
                    JsonOutput.toJson(data.assignees),
                    JsonOutput.toJson(data.tags),
                    Sql.LONGVARCHAR
                ])
                // append the new ticket id which was returned by stored procedure
                data.customerTicketId = customerTicketId
                break
        }

        return data
    }


    def callAPI(MergeAPIParameters params) {
        def response = null
        HttpEntity<String> request
        HttpHeaders headers = new HttpHeaders()
        headers.setBearerAuth(mergeAPIKey)
        headers.add("X-Account-Token", params.accountToken)

        switch( params.method ) {

            case 'GET':
                 request = new HttpEntity<>(headers)

                // make sure we have page_size
                if( !params.params.page_size ) {
                    params.params.page_size = 100
                }

                // convert parameters to query string
                Map<String,String> queryParams = [:]

                params.params.each { key, value ->
                    queryParams[key] = value
                }
                def queryString = params.params.collect { key, value ->
                    if(value instanceof List) {
                        "${key}=${value.join(',')}"
                    } else {
                        "${key}=${value}"
                    }
                }.join('&')

                response = restTemplate.exchange("$params.fullAPIURL?$queryString", HttpMethod.GET, request, String.class)

                if( response instanceof Map && response.statusCode != HttpStatus.OK && !response.body.size() ) {
                    // TODO: some error checking
                }

                return response.body

                break

            case "POST":
            case "PATCH":
                headers.setContentType(MediaType.APPLICATION_JSON)

                // build the 'model' object that will be posted to api
                Map body = [ model: params.params ]

                request = new HttpEntity<>(new JsonBuilder(body).toPrettyString(), headers)

                if( params.method == 'POST') {
                    response = restTemplate.postForObject(params.fullAPIURL, request, Map.class)
                } else {
                    // override the header to make a 'patch'
                    restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory())
                    response = restTemplate.patchForObject(params.fullAPIURL, request, Map.class)
                }
                if( response instanceof Map && response.errors && response.errors.length() ) {
                    // TODO: some error checking
                }

                // send back the model data
                return response.model

                break
        }

        // safe return
        return null
    }

    private getData(MergeAPIParameters params) {
        String next = '1'
        String results = ""
        List allData = []
        def data = null

        while (next?.length() > 0) {
            results = callAPI(params)
            if (results?.length() > 0) {
                data = new JsonSlurper().parseText(results)
                next = data.next
                if (next && next != 1) {
                    params.params.cursor = next
                }
                allData.addAll(data.results)
            }
        }
        return allData
    }

}
