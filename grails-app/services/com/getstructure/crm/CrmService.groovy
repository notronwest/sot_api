package com.getstructure.crm

import com.getstructure.GSAbstractService
import com.getstructure.crm.model.HubspotIntakePanelRequest
import com.getstructure.crm.model.HubspotIntakePanelResponse
import com.getstructure.crm.model.HubspotOauthRedirectRequest
import com.getstructure.crm.model.HubspotOauthRedirectResponse
import com.getstructure.exception.ErrorCode
import com.getstructure.exception.GSException
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.sql.Sql
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

import javax.servlet.http.HttpServletRequest

class CrmService extends GSAbstractService{

    static final String hubspotApiEndpoint = 'https://api.hubapi.com'
    static final String initialAccessTokenEndpoint = '/oauth/v1/token'
    static final String accessTokenEndpoint = '/oauth/v1/access-tokens'
    static final String refresTokenEndpoint = '/oauth/v1/refresh-tokens'
    // TODO: move to .env
    static final String clientId = 'f377ded0-c059-4a60-b544-83aad16d6dbc'
    static final String clientSecret = '10cf656c-7c00-45bf-b2ed-5918b69d3e0f'
    private final RestTemplate restTemplate = new RestTemplate()

    HubspotOauthRedirectResponse handleHubspotRedirect(HubspotOauthRedirectRequest request) {
        // auth

        // operation
        // get the software_id for hubspot
        List softwareList = sql.rows("SELECT id FROM getstructure.software WHERE name = 'Hubspot'")
        if ( !softwareList || softwareList.isEmpty() ) {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Error initializing hubspot redirect api call")
        }
        Map tokens = getAccessTokenFromCode(request.code)

        if( !tokens || !tokens.access_token || !tokens.refresh_token ) {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Tokens not present in response: ${tokens.toString()}")
        }

        // store the access token
        sql.call('CALL saveSoftwareOauthToken_gs(?,?,?,?,?,?,?)', [
                null,
                null,
                softwareList.collect { it.id }.first(),
                null,
                tokens.access_token,
                tokens.refresh_token,
                Sql.LONGVARCHAR
        ])

        return new HubspotOauthRedirectResponse()
    }

    HubspotIntakePanelResponse getIntakePanel(HubspotIntakePanelRequest request) {
        // auth

        // operation
        Map loadLead = [
            type: 'CONFIRMATION_ACTION_HOOK',
            httpMethod: 'POST',
            uri: 'https://api.getstructure.com/api/crm/hubspotLoadCustomerLead',
            label: 'Load Lead into SOT',
            associatedObjectProperties: [
                    'company','name','address','address2','city','state','zip','numberofemployees'
            ],
            confirmationMessage: 'Are you sure you would like to load this lead into the source of truth?',
            confirmButtonText: 'Yes',
            cancelButtonText: 'No'
        ]
        Map intakeForm = [
                type: 'IFRAME',
                width: 890,
                height: 748,
                uri: 'https://getstructure.retool.com/apps/9c0df34a-a669-11ed-b1aa-87a6a41e6567/Lead%20Management/Lead%20Intake%20Form',
                label: 'Open Intake Form',
                associatedObjectProperties: [ 'company','name','address','address2','city','state','zip','numberofemployees' ]
        ]

        // see if this company has been loaded yet
        List lead = sql.rows("""
            SELECT COUNT('x') as hasLead
            FROM customer_lead
            WHERE also_known_as = :name
        """, [ name: request.name ])

        return new HubspotIntakePanelResponse(
            results: [],
            totalCount: lead && !lead.isEmpty() && lead.first().hasLead ? 1 : 0,
            // allItemsLink: '',
            // itemLabel: '',
            // settingsAction: [:],
            primaryAction: lead && !lead.isEmpty() && lead.first().hasLead ? intakeForm : loadLead
        )
    }

    Map hubspotLoadCustomerLead(HubspotIntakePanelRequest intakePanelRequest, HttpServletRequest request) {
        // auth

        // operation
// debug
def debug = [
    name: intakePanelRequest.name,
    associatedObjectProperties: intakePanelRequest.associatedObjectId,
    associatedObjectType: intakePanelRequest.associatedObjectType

]
        // save the lead
        try {
            sql.call("{CALL loadCustomerLeadFromCRM_gs(?,?,?)}", [
                intakePanelRequest.name ? intakePanelRequest.name : 'Error',
                JsonOutput.toJson(debug),
                Sql.LONGVARCHAR
            ], {errorMessageCode ->
                if( errorMessageCode ) {
                    throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Error calling db: ${errorMessageCode.toString()}")
                }
            })
        } catch ( Error e) {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Unable to save hubspot lead: ${e.toString()}")
        }

        return [ message: "Lead successfully added to source of truth. You should now be able to complete the Intake Form. Refresh the page if you don't see this option"]
    }

    private Map getAccessTokenFromCode(String code) {
        // auth

        // operation
        Map response = [:]
        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)
        // build the bpdy that will be posted to api

        def requestBody = new LinkedMultiValueMap<String, String>()
        requestBody.add("grant_type", "authorization_code")
        requestBody.add("client_id", clientId)
        requestBody.add("client_secret", clientSecret)
        requestBody.add("redirect_uri", "https://api.getstructure.com/api/crm/hubspotRedirectHandler")
        requestBody.add("code", code)


        try {
            response = restTemplate.postForObject(hubspotApiEndpoint + initialAccessTokenEndpoint, requestBody, Map.class, headers)
        } catch( Error e) {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Error retrieving token for request: ${requestBody.toString()}")
        }

        return response
    }
}
