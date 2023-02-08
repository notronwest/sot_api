package com.getstructure.exception

enum ErrorCode {
    GS000('GS000', 'Server Error Occurred'),
    GS001('GS001', 'Property with name :propertyName is mandatory in request.'),
    GS002('GS002','Entity not found'),
    GS003('GS003','Unprocessable Request'),
    GS004('GS004','User with that email already exists'),
    GS005( 'GS005','Illegal access' ),
    GS006( 'GS006','Database error occurred' )

    private final String code
    private final String message

    ErrorCode(String code, String message) {
        this.code = code
        this.message = message
    }

    String getCode() {
        return code
    }

    String getMessage() {
        return message
    }


    String toString() {
        return name() + " = (" + code + ", " + message + ")"
    }
}
