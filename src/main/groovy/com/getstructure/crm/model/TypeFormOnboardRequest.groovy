package com.getstructure.crm.model

import grails.validation.Validateable

class TypeFormOnboardRequest implements Validateable {
    String event_id
    String event_type
    Response form_response

        class Response {
            String form_id
            String token
            String landed_at
            String submitted_at
            Hidden hidden
            Definition definition
            List<Map> answers

            class Hidden {
                String customer_name
            }

            class Definition {
                String id
                String title
                List<Fields> fields

                class Fields {
                    String id
                    String ref
                    String type
                    String title
                    Properties properties
                    Boolean allow_other_choice
                    List<Choices> choices

                    class Properties {}

                    class Choices {
                        String id
                        String label
                    }
                }
            }

            class Answer {
                Field field
                String type
                String text
                String email
                String file_url
                String choice
                List<String> choices

                class Field {
                    String id
                    String type
                    String ref
                }
            }
        }
}
