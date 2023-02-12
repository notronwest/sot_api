package com.getstructure.email

import com.getstructure.GSAbstractService
import com.getstructure.email.model.SaveEmailRequest
import com.getstructure.email.model.SaveEmailResponse
import com.getstructure.exception.ErrorCode
import com.getstructure.exception.GSException
import com.getstructure.util.GSDateUtil
import grails.gorm.transactions.Transactional
import groovy.json.JsonOutput
import groovy.sql.Sql
import org.springframework.http.HttpStatus

@Transactional
class EmailService extends GSAbstractService {

    SaveEmailResponse save( SaveEmailRequest saveEmailRequest ) {
        // auth

        // operation
        List customerEmailId = []
        try {
            customerEmailId = sql.callWithRows("CALL saveCustomerEmail_gs(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", [
                    saveEmailRequest.customerEmailId,
                    saveEmailRequest.customerId,
                    saveEmailRequest.emailLocator,
                    saveEmailRequest.messageId,
                    saveEmailRequest.status,
                    saveEmailRequest.subject,
                    JsonOutput.toJson(saveEmailRequest.toRecipients),
                    saveEmailRequest.senderName,
                    saveEmailRequest.senderEmail,
                    JsonOutput.toJson(saveEmailRequest.ccRecipients),
                    JsonOutput.toJson(saveEmailRequest.bccRecipients),
                    saveEmailRequest.sentDate ? GSDateUtil?.formatDateDBDateTime(saveEmailRequest.sentDate) : null,
                    saveEmailRequest.isFounderEmail,
                    saveEmailRequest.isVendorEmail,
                    saveEmailRequest.vendorId,
                    saveEmailRequest.vendorDetails ? JsonOutput.toJson(saveEmailRequest.vendorDetails) : '{}',
                    saveEmailRequest.attachments ? JsonOutput.toJson(saveEmailRequest.attachments) : '[]',
                    saveEmailRequest.metadata ? JsonOutput.toJson(saveEmailRequest.metadata) : '{}',
                    saveEmailRequest.tags ? JsonOutput.toJson(saveEmailRequest.tags) : '[]',
                    saveEmailRequest.rules ? JsonOutput.toJson(saveEmailRequest.rules) : '[]',
                    saveEmailRequest.isTest ? saveEmailRequest.isTest : 0,
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
