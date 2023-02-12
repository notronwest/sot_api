package com.getstructure.oauth

import com.getstructure.GSAbstractController
import com.getstructure.oauth.model.OauthRedirectHandlerRequest
import grails.converters.JSON

class OauthController extends GSAbstractController{

    OauthService oauthService

    def redirectHandler(OauthRedirectHandlerRequest oauthRedirectHandlerRequest) {
        render oauthService.handleRedirect(oauthRedirectHandlerRequest) as JSON
    }

    def finchConnect() {
        render (view: '/finchConnect.gsp')
    }

}
