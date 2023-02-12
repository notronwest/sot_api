package com.getstructure.oauth.model

import grails.validation.Validateable

class OauthRedirectHandlerRequest implements Validateable {
    String code
    String state
}
