package com.getstructure.email.model

import grails.validation.Validateable
import org.joda.time.DateTime

class SaveEmailRequest implements Validateable{
    String customerEmailId
    String customerId
    String emailLocator
    String messageId
    String status
    String subject
    List<Map> toRecipients
    String senderEmail
    String senderName
    List<Map> ccRecipients
    List<Map> bccRecipients
    String sentDate
    Integer isFounderEmail
    Integer isVendorEmail
    String vendorId
    Map<String,String> vendorDetails
    List attachments
    Map<String,String> metadata
    List<Map> rules
    List tags
    Integer isTest

    static constraints = {
        customerEmailId nullable: true
        customerId nullable: false
        emailLocator nullable: false
        messageId nullable: false
        status nullable: true
        subject nullable: false
        toRecipients nullable: false
        senderEmail nullable: false
        senderName nullable: true
        ccRecipients nullable: true
        bccRecipients nullable: true
        sentDate nullable: false
        isFounderEmail nullable: true
        isVendorEmail nullable: true
        vendorId nullable: true
        vendorDetails nullable: true
        attachments nullable: true
        metadata nullable: true
        rules nullable: true
        tags nullable: true
        isTest nullable: true
    }
}
