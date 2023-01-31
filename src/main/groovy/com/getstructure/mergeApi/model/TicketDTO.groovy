package com.getstructure.mergeApi.model

import grails.validation.Validateable

class TicketDTO implements Validateable {
    String customerId
    String ticketId
    String remoteId
    String parentId
    String status
    String priority
    String ticketType
    String name
    String description
    String dueDate
    String createdAt
    String updatedAt
    String completedAt
    String collectionIds
    String assignees
    String tags
}
