package com.getstructure.customer

import com.getstructure.GSAbstractController
import com.getstructure.customer.model.CustomerDetailRequest
import grails.converters.JSON

class CustomerController extends GSAbstractController {
    CustomerService customerService

    def getDetails (CustomerDetailRequest request){
        render customerService.getDetails(request) as JSON
    }

}
