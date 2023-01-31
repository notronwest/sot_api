package com.getstructure.exception

class GSAuthenticationException extends RuntimeException {

    GSAuthenticationException() {
        super("Authentication failed")
    }
}
