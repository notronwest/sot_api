package com.getstructure.customer

import com.getstructure.GSAbstractService
import com.getstructure.customer.model.CustomerDetailRequest
import com.getstructure.customer.model.CustomerDetailResponse
import com.getstructure.exception.ErrorCode
import com.getstructure.exception.GSException
import org.springframework.http.HttpStatus

class CustomerService extends GSAbstractService {

    CustomerDetailResponse getDetails(CustomerDetailRequest customerDetailRequest ) {
        // auth

        // operation
        List customerDetailList = []
        CustomerDetailResponse customerDetailResponse = new CustomerDetailResponse()
        List params = [ customerDetailRequest?.customerId, customerDetailRequest?.customerName ]
        try {
            customerDetailList = sql.callWithRows("CALL getCustomerDetail_gs(?,?)", params, {})

            if( customerDetailList && !customerDetailList.isEmpty() ) {
                customerDetailResponse = customerDetailList.collect {
                    new CustomerDetailResponse(
                            customerId: it.id,
                            entityName: it.name,
                            alsoKnownAs: it.also_known_as,
                            startDate: it.start_date,
                            ein: it.ein,
                            corpEmail: it.corp_email,
                            inGSEmail: it.in_gs_email,
                            arrowsPlanLink: it.arrows_plan_link,
                            slackChannel: it.slack_channel,
                            fileUploadURL: it.file_upload_url,
                            remoteTicketCollectionId: it.remote_ticket_collection_id,
                            founderEmail: it.founder_email
                    )
                }.first()
            } else {
                throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS003, "Error getting data for: ${customerDetailRequest.customerName ? customerDetailRequest.customerName : customerDetailRequest.customerId}")
            }

        } catch ( Error e) {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS000, "There was an error getting the customer details")
        }

        return customerDetailResponse
    }


}
