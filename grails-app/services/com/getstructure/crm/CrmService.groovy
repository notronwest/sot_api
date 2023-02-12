package com.getstructure.crm

import com.getstructure.GSAbstractService
import com.getstructure.crm.model.HubspotIntakePanelRequest
import com.getstructure.crm.model.HubspotIntakePanelResponse
import com.getstructure.crm.model.HubspotOauthRedirectRequest
import com.getstructure.crm.model.HubspotOauthRedirectResponse
import com.getstructure.crm.model.HubspotSearchFilterDTO
import com.getstructure.crm.model.SOTOnboardingDTO
import com.getstructure.crm.model.TypeFormOnboardRequest
import com.getstructure.exception.ErrorCode
import com.getstructure.exception.GSException
import grails.gorm.transactions.Transactional
import groovy.json.JsonOutput
import groovy.sql.Sql
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Transactional
class CrmService extends GSAbstractService{

    static final String hubspotSoftwareName = 'hubspot'
    static final String hubspotApiEndpoint = 'https://api.hubapi.com'
    static final String accessTokenEndpoint = '/oauth/v1/token'
    static final String hubspotSearchCompaniesEndpoint = '/crm/v3/objects/companies/search'
    // TODO: move to .env
    static final String clientId = 'f377ded0-c059-4a60-b544-83aad16d6dbc'
    static final String clientSecret = '10cf656c-7c00-45bf-b2ed-5918b69d3e0f'
    private final RestTemplate restTemplate = new RestTemplate()

    HubspotOauthRedirectResponse handleHubspotRedirect(HubspotOauthRedirectRequest request) {
        // auth

        // operation
        // get the software_id for hubspot
        String query = """SELECT id FROM getstructure.software WHERE name = :softwareName"""
        List softwareList = sql.rows(query, [ softwareName: hubspotSoftwareName ])
        if ( !softwareList || softwareList.isEmpty() ) {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Error initializing hubspot redirect api call")
        }
        Map tokens = getAccessTokenFromCode(request.code)

        if( !tokens || !tokens.access_token || !tokens.refresh_token ) {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Tokens not present in response: ${tokens.toString()}")
        }

        // store the access token
        sql.call('CALL saveSoftwareOauthToken_gs(?,?,?,?,?,?,?,?)', [
                null,
                null,
                softwareList.collect { it.id }.first(),
                null,
                tokens.access_token,
                tokens.refresh_token,
                tokens.expires_in,
                Sql.LONGVARCHAR
        ])

        return new HubspotOauthRedirectResponse()
    }

    Map hubspotLoadCustomerLead(HubspotIntakePanelRequest intakePanelRequest) {
        // auth

        // operation
        if( !intakePanelRequest.hs_object_id ) {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Invalid API call -- no hs_object_id")
        }
        // get the company name for the deal
        HubspotSearchFilterDTO.FilterDTO filterDTO = new HubspotSearchFilterDTO.FilterDTO()
        filterDTO.propertyName = 'associations.deal'
        filterDTO.operator = 'EQ'
        filterDTO.value = intakePanelRequest.hs_object_id
        // save the lead
        try {
            Map searchResults = searchHubspot(filterDTO)
            if( searchResults && searchResults.results.size() ) {
                sql.call("{CALL loadCustomerLeadFromCRM_gs(?,?,?,?,?,?)}", [
                        Long.parseLong(intakePanelRequest.hs_object_id),
                        Long.parseLong(searchResults.results[0].id),
                        searchResults.results[0].properties.name,
                        intakePanelRequest.userEmail,
                        JsonOutput.toJson([]),
                        Sql.LONGVARCHAR
                ], { errorCodeMessage ->
                    if (errorCodeMessage) {
                        throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Error saving the lead to the db: ${errorCodeMessage.toString()}")
                    }
                })
            } else {
                throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Search for the company associated with the deal failed")
            }
        } catch ( Error e) {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Unable to save hubspot lead: ${e.toString()}")
        }

        return [ message: "Lead successfully added to source of truth. You should now be able to complete the Intake Form. Refresh the page if you don't see this option"]
    }

    HubspotIntakePanelResponse getIntakePanel(HubspotIntakePanelRequest hubspotIntakePanelRequest) {
        // auth

        // operation
        Map loadLead = [
            type: 'CONFIRMATION_ACTION_HOOK',
            httpMethod: 'POST',
            uri: 'https://api.getstructure.com/api/crm/hubspotLoadCustomerLead',
            label: 'Load Deal/Company into SOT',
            associatedObjectProperties: ['hs_object_id','dealname'],
            confirmationMessage: 'Are you sure you would like to load this deal/company into the source of truth?',
            confirmButtonText: 'Yes',
            cancelButtonText: 'No'
        ]
        Map intakeForm = [
            type: 'IFRAME',
            width: 890,
            height: 748,
            uri: 'https://getstructure.retool.com/apps/9c0df34a-a669-11ed-b1aa-87a6a41e6567/Lead%20Management/Lead%20Intake%20Form',
            label: 'Open Intake Form',
            associatedObjectProperties: [ 'hs_object_id','dealname' ]
        ]

        // see if this company has been loaded yet
        String query = """
            SELECT COUNT('x') as hasLead
            FROM customer_lead
            WHERE crm_deal_id = :crmDealId
        """
        List lead = sql.rows(query, [ crmDealId: hubspotIntakePanelRequest.hs_object_id ])

        return new HubspotIntakePanelResponse(
            results: [],
            totalCount: lead && !lead.isEmpty() && lead.first().hasLead ? 1 : 0,
            // allItemsLink: '',
            // itemLabel: '',
            // settingsAction: [:],
            primaryAction: lead && !lead.isEmpty() && lead.first().hasLead ? intakeForm : loadLead
        )
    }

    def typeFormOnboardingHandler(TypeFormOnboardRequest typeFormOnboardRequest) {
        // auth

        // operation
        // convert the form response into DTO for storage
        SOTOnboardingDTO sotOnboardingDTO = SOTOnboardingDTO.fromTypeFormOnboardRequest(typeFormOnboardRequest)
        Map customerLead = [:]
        String customerId = ''
        // make sure we have a customer name
        if( sotOnboardingDTO.entityName ) {
            // get the lead for this company
            // List customerLeadList = sql.callWithRows('CALL getCustomerLeadByName_gs(?)', [sotOnboardingDTO.alsoKnownAs], {})
            /* if( customerLeadList && !customerLeadList.isEmpty() ) {
                customerLead = customerLeadList.first()
                // see if this deal is already a customer
                if( !customerLead.customer_id ) {
                    // create the customer
                    List customerIdList = sql.callWithRows('CALL moveLeadToCustomer_gs(?,?,?)', [
                        customerLead.id,
                        null,
                        Sql.LONGVARCHAR
                    ], {})

                } else {
                    // somehow this was set in the lead without being onboarded ... will need to look into this
                    customerId = customerLead.customer_id
                } */
                List customerIdList = sql.callWithRows('CALL createCustomerFromOnboarding_gs(?,?)', [
                    sotOnboardingDTO.entityName,
                    Sql.LONGVARCHAR
                ], {errorCodeMessage ->
                    if( errorCodeMessage ) {
                        throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Error saving the customer record to the db: ${errorCodeMessage.toString()}")
                    }
                })

                // get the customer id to use later
                if( customerIdList && !customerIdList.isEmpty() ){
                    // set the customerId so it can be used for the onboarding
                    customerId = customerIdList.first().customerId
                }
                // if we got this far and still don't have a customer bail -- in the future maybe we just create a ticket?
                if( !customerId || !customerId.length() ) {
                    throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Could not create customer record for deal connected to: ${sotOnboardingDTO.entityName}")
                }

            /*} else {
                throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Could not match ${sotOnboardingDTO.entityName} to a deal")
            }*/
            // see if we have an onboarding record for them already
            List customerOnboarding = sql.callWithRows('CALL getCustomerOnboarding_gs(?)', [ customerId ], {})
            if( customerOnboarding && !customerOnboarding.isEmpty() ) {
                // load the non-type form data from the database into the onboarding map
                customerOnboarding.collect{
                    sotOnboardingDTO.customerOnboardingId = it.id
                    sotOnboardingDTO.mailManagement = it.mail_management
                    sotOnboardingDTO.businessInsurances = it.business_insurances
                    sotOnboardingDTO.registeredAgentService = it.registered_agent_service
                    sotOnboardingDTO.countriesAndStates = it.countries_and_states
                    sotOnboardingDTO.howSell = it.how_sell
                    sotOnboardingDTO.paymentProcess = it.payment_process
                    sotOnboardingDTO.runwayMrr = it.runway_mrr
                    sotOnboardingDTO.contractorAgreements = it.contractor_agreements
                    sotOnboardingDTO.employeeAgreements = it. employee_agreements
                    sotOnboardingDTO.proprietaryInfoAgreements = it.proprietary_info_agreements
                    sotOnboardingDTO.peopleBasedInsurance = it.people_based_insurance
                    sotOnboardingDTO.threeMonthsBanking = it.three_months_banking
                    sotOnboardingDTO.bankStatementsDue = it.bank_statements_due
                    sotOnboardingDTO.threeMonthsCC = it.three_months_cc
                    sotOnboardingDTO.ccStatementsDue = it.cc_statements_due
                }
            }
            // store the data
            sql.call('CALL saveCustomerOnboarding_gs(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)',
                [
                    sotOnboardingDTO.customerOnboardingId,
                    customerId,
                    null, // unfortunately, we do not collect who is filling out the form or completing arrows flow
                    sotOnboardingDTO.entityName,
                    sotOnboardingDTO.alsoKnownAs,
                    sotOnboardingDTO.mailManagement,
                    sotOnboardingDTO.businessInsurances,
                    sotOnboardingDTO.registeredAgentService,
                    sotOnboardingDTO.countriesAndStates,
                    sotOnboardingDTO.howSell,
                    sotOnboardingDTO.paymentProcess,
                    sotOnboardingDTO.runwayMrr,
                    sotOnboardingDTO.contractorAgreements,
                    sotOnboardingDTO.employeeAgreements,
                    sotOnboardingDTO.proprietaryInfoAgreements,
                    sotOnboardingDTO.peopleBasedInsurance,
                    sotOnboardingDTO.threeMonthsBanking,
                    sotOnboardingDTO.bankStatementsDue,
                    sotOnboardingDTO.threeMonthsCC,
                    sotOnboardingDTO.ccStatementsDue,
                    sotOnboardingDTO.workEnvironment,
                    sotOnboardingDTO.differentAddress,
                    sotOnboardingDTO.secureDocumentSoftware,
                    sotOnboardingDTO.secureDocumentURL,
                    sotOnboardingDTO.regulatoryIssues,
                    sotOnboardingDTO.bankName,
                    sotOnboardingDTO.ccCompany,
                    sotOnboardingDTO.financialModelDevelopment,
                    sotOnboardingDTO.numberOfEmployees,
                    sotOnboardingDTO.payrollSoftware,
                    sotOnboardingDTO.numberOfContractors,
                    sotOnboardingDTO.contractorLocation,
                    sotOnboardingDTO.contractorPaymentProcess,
                    sotOnboardingDTO.passwordManager,
                    Sql.LONGVARCHAR
                ], { errorCodeMessage ->
                if( errorCodeMessage ) {
                    throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Error saving the onboarding record to the db: ${errorCodeMessage.toString()}")
                }
            })
        } else {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Type form request was not valid")
        }
    }

    private Map searchHubspot(HubspotSearchFilterDTO.FilterDTO filter) {
        // auth

        // operation
        // build the filtering
        HubspotSearchFilterDTO searchFilter = new HubspotSearchFilterDTO()
        HubspotSearchFilterDTO.FilterGroupDTO filterGroupDTO = new HubspotSearchFilterDTO.FilterGroupDTO()
        searchFilter.filterGroups = [filterGroupDTO]
        filterGroupDTO.filters = [filter]

        HttpHeaders headers = new HttpHeaders()
        headers.setContentType(MediaType.APPLICATION_JSON)
        headers.setBearerAuth(getAccessTokenForPlatform(hubspotSoftwareName))

        def entity = new HttpEntity<>(searchFilter, headers)

        return restTemplate.postForObject(hubspotApiEndpoint + hubspotSearchCompaniesEndpoint, entity, Map.class)

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
            response = restTemplate.postForObject(hubspotApiEndpoint + accessTokenEndpoint, requestBody, Map.class, headers)
        } catch( Error e) {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Error retrieving token for request: ${requestBody.toString()}")
        }

        return response
    }

    private String getAccessTokenForPlatform(String platformName) {
        // auth

        // operation
        Map platformOauthToken = [:]
        Map newAccessToken = [ access_token: '']
        // get the auth token for the software
        List tokenList = sql.callWithRows('CALL getstructure.getPlatformOauthToken_gs(?)', [ platformName ], {})
        if( tokenList && !tokenList.isEmpty() ) {
            platformOauthToken = tokenList.first()
            // see if the access token is valid
            def expiryTime = new Date(platformOauthToken.granted_on.getTime() + 1800 * 1000)
            // the token is expired
            if(  expiryTime < new Date()  ) {

                HttpHeaders headers = new HttpHeaders()
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                // build the bpdy that will be posted to api

                def requestBody = new LinkedMultiValueMap<String, String>()
                requestBody.add("grant_type", "refresh_token")
                requestBody.add("client_id", clientId)
                requestBody.add("client_secret", clientSecret)
                requestBody.add("refresh_token", platformOauthToken.refresh_token)


                try {
                    newAccessToken = restTemplate.postForObject(hubspotApiEndpoint + accessTokenEndpoint, requestBody, Map.class, headers)

                    if( newAccessToken.access_token ) {
                        sql.call('CALL getstructure.saveSoftwareOauthToken_gs(?,?,?,?,?,?,?,?)', [
                                platformOauthToken.id,
                                null, // ignored
                                null, // ignored
                                null, // ignored
                                newAccessToken.access_token,
                                platformOauthToken.refresh_token,
                                newAccessToken.expires_in,
                                Sql.LONGVARCHAR
                        ])
                    }
                } catch( Error e) {
                    throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Error retrieving token for request: ${requestBody.toString()}")
                }
            } else {
                newAccessToken.access_token = platformOauthToken.access_token
            }

        } else {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Error retrieving platform details}")
        }


        return newAccessToken.access_token
    }
}
