package com.getstructure

import grails.converters.JSON
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource

@Slf4j
class HealthController {

    Sql sql

    @Value("classpath:deployment-properties.json")
    Resource deploymentPropertiesResource

    private Object _deploymentProperties
    protected getDeploymentProperties() {
        if ( !_deploymentProperties ) {
            try {
                _deploymentProperties = new JsonSlurper().parse( deploymentPropertiesResource.file )
            } catch ( Exception e ) {
                log.error("Unable to read config.json", e )
                _deploymentProperties = [
                        build: "Unknown; exception reading config."
                ]
            }
        }

        return _deploymentProperties
    }


    def index() {
        Map model = [
                ds: [:]
        ]

        Map sqlDef = [
                 label: 'SQL', now: null
        ]
        try {
            sqlDef.now = sql.firstRow("select now() as now").now
        } catch ( Throwable t ) {
            log.error("Error trying to communicate with ${sqlDefinition.type} Sql", )
        }
        model.ds[ sqlDef.label ] = ( sqlDef.now != null )
        model.build = deploymentProperties.build
        model.version = deploymentProperties.version
        render model as JSON
    }
}
