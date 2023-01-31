package com.getstructure

import grails.converters.JSON

class HomeController {

    def index() {
        Map status = [:]
        render status as JSON
    }
}
