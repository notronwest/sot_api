package com.getstructure.email

import com.getstructure.GSAbstractController
import com.getstructure.email.model.SaveEmailRequest
import grails.converters.JSON

class EmailController extends GSAbstractController {
    EmailService emailService

    def save(SaveEmailRequest request){
        render emailService.save(request) as JSON
    }

}
