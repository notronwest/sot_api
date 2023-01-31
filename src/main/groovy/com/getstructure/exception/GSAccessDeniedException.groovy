package com.getstructure.exception


class GSAccessDeniedException extends RuntimeException {
    GSAccessDeniedException() {
        super("Permission denied")

    }
}
