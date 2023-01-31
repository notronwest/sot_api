package com.getstructure.MergeAuth

import com.getstructure.mergedev.MergeApiController
import grails.testing.web.controllers.ControllerUnitTest
import groovy.json.JsonSlurper
import spock.lang.Specification

class MergeAuthControllerSpec extends Specification implements ControllerUnitTest<MergeApiController> {

    def setup() {
    }

    def cleanup() {
    }

    void "test json parsing mf"() {
        given:
        String jsonText = '{"model":{"id":"20e91365-0873-4fed-b364-780347225113","remote_id":"860pr1f1h","name":"Do something great","assignees":["b0a36fa9-c9fd-4862-9653-326f62fb1c24"],"creator":null,"due_date":"2023-02-06T12:00:00Z","status":"OPEN","description":"Here are the things we need to do","project":"28380bb4-2e9d-4964-9057-e199cfb24194","collections":["d54072f5-6462-47c8-8ac4-dc5807fc157b"],"ticket_type":null,"account":null,"contact":null,"parent_ticket":null,"attachments":[],"tags":[],"remote_created_at":"2023-01-30T05:00:07.131000Z","remote_updated_at":"2023-01-30T05:00:07.131000Z","completed_at":null,"remote_was_deleted":false,"ticket_url":"https://app.clickup.com/t/860pr1f1h","priority":null,"field_mappings":null,"remote_data":null},"warnings":[],"errors":[]}'

        when:
        Map jsonMap = new JsonSlurper().parseText(jsonText) as Map

        then:
        jsonMap.model.id == '20e91365-0873-4fed-b364-780347225113'
    }
}
