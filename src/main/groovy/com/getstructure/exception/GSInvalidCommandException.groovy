package com.getstructure.exception

import grails.converters.JSON
import grails.validation.Validateable
import groovy.json.JsonSlurper

class GSInvalidCommandException extends RuntimeException {
    Map errors

    GSInvalidCommandException(Validateable validateable) {
        super("Invalid Command")

        // Converting a Validateable's "errors" to JSON gets Grails to resolve its error messages using messages.properties
        String errorJson = validateable.errors as JSON
        Object resolvedErrors = new JsonSlurper().parseText(errorJson)

        // And now we can reduce this to a tidy packet, not returning all of the invalid values, which could be megabytes of text!
        errors = [:]
        resolvedErrors.errors?.each { Map errorMap ->
            if (!errors.containsKey(errorMap.field)) {
                errors[errorMap.field] = []
            }
            errors[errorMap.field] << errorMap.message
        }

    }
}
