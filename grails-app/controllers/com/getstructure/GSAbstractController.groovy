package com.getstructure


import com.getstructure.exception.GSAccessDeniedException
import com.getstructure.exception.GSAuthenticationException
import com.getstructure.exception.GSException
import com.getstructure.exception.GSInvalidCommandException
import com.getstructure.exception.GSNotFoundException
import com.getstructure.exception.model.ApiExceptionResponse
import grails.converters.JSON

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.NOT_FOUND

class GSAbstractController {
    Boolean onTestServer = false


    /******************************************************************************/
    /** Conventional exception handling                                           */
    /******************************************************************************/

    def handleTaAccessDeniedException( final GSAccessDeniedException exception ) {
        response.status = FORBIDDEN.value()

        if ( onTestServer ) {
            log.error("(Development only, for debugging help): GSAccessDeniedException thrown", exception)
        }
        render new ApiExceptionResponse( message: exception.message ) as JSON
    }

    def handleGSAuthenticationException( final GSAuthenticationException exception ) {
        response.status = FORBIDDEN.value()

        if ( onTestServer ) {
            log.error("(Development only, for debugging help): GSAuthenticationException thrown", exception)
        }
        render new ApiExceptionResponse( message: exception.message ) as JSON
    }

    def handleGSNotFoundException( final GSNotFoundException exception ) {
        response.status = NOT_FOUND.value()

        if ( onTestServer ) {
            log.error("(Development only, for debugging help): GSNotFoundException thrown", exception)
        }
        render new ApiExceptionResponse( message: exception.message ) as JSON
    }

    def handleGSInvalidCommandException( final GSInvalidCommandException exception ) {
        response.status = BAD_REQUEST.value() // we can argue about BAD_REQUEST vs. UNPROCESSABLE_ENTITY sometime, but I side with this post: https://stackoverflow.com/questions/16133923/400-vs-422-response-to-post-of-data
        render new ApiExceptionResponse( message: exception.message, errors: exception.errors ) as JSON
    }

    def handleTherapyAppointmentException( final GSException gse ) {
        log.error( "An error occurred in an API call: it is logged below.", gse )
        String message
        message = gse.getErrorMessage()
        if (gse.messageLiterals && message)
            gse.messageLiterals.each { key, value -> message = message.replace(":$key", value as String) }
        GSResponse reportResponse = new GSResponse(data: ['errorCode': gse.errorCode, 'errorMessage': message, 'detailMessage': gse.getMessage()])
        render reportResponse as JSON, status: gse.getHttpStatus()
    }

    def handleException( final Exception exception ) {
        log.error( "An error occurred in an API call: it is logged below.", exception )
        response.status = 500
        render new ApiExceptionResponse( message: exception.message ) as JSON
    }
}
