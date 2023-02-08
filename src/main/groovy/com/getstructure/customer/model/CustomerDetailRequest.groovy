package com.getstructure.customer.model

import grails.validation.Validateable

class CustomerDetailRequest implements Validateable {
    String customerId
    String customerName

    static constraints = {
        customerId nullable: true
        customerName nullable: true
    }
}
