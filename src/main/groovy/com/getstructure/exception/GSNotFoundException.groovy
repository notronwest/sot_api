package com.getstructure.exception

/**
 * Generic, no-detail "not found" exception.
 */
class GSNotFoundException extends RuntimeException {

    GSNotFoundException() {
        super("Not found")
    }

    GSNotFoundException(String message) {
        super(message)
    }

}
