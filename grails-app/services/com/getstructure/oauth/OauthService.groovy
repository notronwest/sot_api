package com.getstructure.oauth

import com.getstructure.GSAbstractService
import com.getstructure.crm.model.HubspotOauthRedirectResponse
import com.getstructure.exception.ErrorCode
import com.getstructure.exception.GSException
import com.getstructure.oauth.model.OauthRedirectHandlerRequest
import com.getstructure.oauth.model.OauthRedirectHandlerResponse
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap

@Transactional
class OauthService extends GSAbstractService{

    OauthRedirectHandlerResponse handleRedirect(OauthRedirectHandlerRequest oauthRedirectHandlerRequest){
        // auth

        // operation
        // get the software_id for hubspot
        String query = """
            SELECT id
            FROM getstructure.software
            WHERE name = :softwareName
        """
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

        return new OauthRedirectHandlerResponse()
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

}
