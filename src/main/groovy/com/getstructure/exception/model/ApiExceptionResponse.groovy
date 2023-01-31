package com.getstructure.exception.model

class ApiExceptionResponse {
    String message
    Map<String, List> errors
    String errorCode
    String detailMessage
}
