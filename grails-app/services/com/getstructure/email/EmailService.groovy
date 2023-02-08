package com.getstructure.email

import com.getstructure.GSAbstractService
import com.getstructure.email.model.SaveEmailRequest
import com.getstructure.email.model.SaveEmailResponse
import com.getstructure.exception.ErrorCode
import com.getstructure.exception.GSException
import com.getstructure.util.GSDateUtil
import groovy.json.JsonOutput
import groovy.sql.Sql
import org.springframework.http.HttpStatus

class EmailService extends GSAbstractService {

    SaveEmailResponse save( SaveEmailRequest request ) {
        // auth

        // operation
        List customerEmailId = []
        try {
            customerEmailId = sql.callWithRows("CALL saveCustomerEmail_gs(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", [
                    request.customerEmailId,
                    request.customerId,
                    request.emailLocator,
                    request.messageId,
                    request.status,
                    request.subject,
                    JsonOutput.toJson(request.toRecipients),
                    request.senderName,
                    request.senderEmail,
                    JsonOutput.toJson(request.ccRecipients),
                    JsonOutput.toJson(request.bccRecipients),
                    request.sentDate ? GSDateUtil?.formatDateDBDateTime(request.sentDate) : null,
                    request.isFounderEmail,
                    request.isVendorEmail,
                    request.vendorId,
                    JsonOutput.toJson(request.vendorDetails),
                    JsonOutput.toJson(request.attachments),
                    JsonOutput.toJson(request.metadata),
                    JsonOutput.toJson(request.tags),
                    JsonOutput.toJson(request.rules),
                    Sql.LONGVARCHAR
                ], {errorCodeMessage ->
                    if( errorCodeMessage ) {
                        throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS006, errorCodeMessage.toString())
                    }
                }
            )
        } catch ( Error e) {
            throw new GSException(HttpStatus.BAD_REQUEST, ErrorCode.GS000, "There was an error saving email")
        }

        return new SaveEmailResponse(
            customerEmailId: customerEmailId.size() && customerEmailId[0].workingCustomerEmailId ? customerEmailId[0].workingCustomerEmailId : null
        )
    }


}
