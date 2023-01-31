package com.getstructure.mergedev

import grails.converters.JSON

class OauthController {

    def redirectHandler() {
        // Enumeration headers = getRequest().getHeaderNames()
        String referer = getRequest().getHeader('referer')
        Map queryString = params
        Map test = [:]
        render test as JSON
    }
}
