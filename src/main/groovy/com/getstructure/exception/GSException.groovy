package com.getstructure.exception

import org.springframework.http.HttpStatus

/**
 * @since   V1.0
 */
class GSException extends Exception {
    String errorCode
    String errorMessage
    Map messageLiterals
    HttpStatus httpStatus

    String getErrorCode() {
        errorCode
    }

    String getErrorMessage() {
        errorMessage
    }

    Map getMessageLiterals() {
        messageLiterals
    }

    HttpStatus getHttpStatus(){
        httpStatus
    }

    /**
     * Default Http Status Code will be {@link org.springframework.http.HttpStatus#INTERNAL_SERVER_ERROR}
     * @param errorCode
     *           enum from {@link ErrorCode}
     * @param message
     *          Detail exception for logging
     * @param messageLiterals
     *          variables to be replaced in error message mention in errorCode.
     *          For example <pre>
     *              {@link ErrorCode#IS001} has variable :propertyName, to replace the value messageLiterals will have value [propertyName:"PROPERTY_NAME"]
     *          </pre>
     */
    GSException(ErrorCode errorCode, String message, Map messageLiterals = [:]) {
        super(message)
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
        this.errorCode = errorCode.code
        this.errorMessage = errorCode.message
        this.messageLiterals = messageLiterals
    }

    /**
     *
     * @param httpStatus
     *          enum from {@link org.springframework.http.HttpStatus}
     * @param errorCode
     *          enum from {@link ErrorCode}
     * @param message
     *          Detail exception for logging
     * @param messageLiterals
     *          variables to be replaced in error message mention in errorCode.
     *          For example <pre>
     *             {@link ErrorCode#IS001} has variable :propertyName, to replace the value messageLiterals will have value [propertyName:"PROPERTY_NAME"]
     *          </pre>
     */
    GSException(HttpStatus httpStatus, ErrorCode errorCode, String message, Map messageLiterals = [:]) {
        super(message)
        this.httpStatus = httpStatus
        this.errorCode = errorCode.code
        this.errorMessage = errorCode.message
        this.messageLiterals = messageLiterals
    }


    @Override
    String toString() {
        String updatedErrorMessage
        updatedErrorMessage = this.getErrorMessage()
        if (this.messageLiterals && updatedErrorMessage)
            this.messageLiterals.each { key, value -> updatedErrorMessage = updatedErrorMessage.replace(":$key", value as String) }
        return "GSException{" +
                "errorCode='" + this.errorCode + '\'' +
                ", errorMessage='" + updatedErrorMessage + '\'' +
                ", httpStatus=" + this.httpStatus +
                ", message=" + this.message +
                '}'
    }
}
