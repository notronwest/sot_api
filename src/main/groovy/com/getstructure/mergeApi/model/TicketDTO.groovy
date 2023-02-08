package com.getstructure.mergeApi.model

import grails.validation.Validateable

class TicketDTO implements Validateable {
    String customer_ticket_id
    String customer_id
    String customer_email_id
    String id // this will be the id of the remote ticket
    String remote_id // this is their additional identifier
    String parent_ticket
    String creator
    String name
    String description
    String due_date
    String remote_created_at
    String remote_updated_at
    String status
    String completed_at
    String ticket_url
    String priority
    String ticket_type
    String collections
    String assignees
    String tags

    static constraints = {
        customer_ticket_id nullable: true
        customer_id nullable: false
        customer_email_id nullable: true
        id nullable: true
        remote_id nullable: true
        parent_ticket nullable: true
        creator nullable: false
        name nullable: false
        description nullable: true
        due_date nullable: true
        remote_created_at nullable: true
        remote_updated_at nullable: true
        status nullable: true
        completed_at nullable: true
        ticket_url nullable: true
        priority nullable: true
        ticket_type nullable: true
        collections nullable: true
        assignees nullable: false
        tags nullable: true
    }

}
