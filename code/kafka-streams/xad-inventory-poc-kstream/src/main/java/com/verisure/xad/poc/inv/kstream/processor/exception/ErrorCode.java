package com.verisure.xad.poc.inv.kstream.processor.exception;

import lombok.Getter;

/**
 * Enumeration of exception error codes.
 * 
 * @since 1.0.0
 * @see com.verisure.xad.poc.inv.kstream.service.exception.ServiceException
 * @see com.verisure.xad.poc.inv.kafkastreamsignalexcess.api.dto.ErrorDTO
 */
@Getter
public enum ErrorCode {

    /**
     * {@code 0 Undefined Error}.
     * <p>Generic code for undefined errors.</p>
     */
    UNDEFINED_ERROR(0, "Undefined error."),

    /**
     * {@code 1 Client Resource validation error}
     * <p>Code used for identifying validation errors when calling to third party clients.</p>
     */
    VALIDATION_ERROR(1, "Client Resource validation error."),

    /**
     * {@code 2 Client Resource not found}
     * <p>Code used for identifying not found resource errors when calling to third party clients.</p>
     */
    RESOURCE_NOT_FOUND(2, "Client Resource not found.");

    /**
     * Numeric value for the error code.
     */
    private final int code;

    /**
     * Human friendly description of the error.
     */
    private final String description;

    /**
     * Private constructor
     * 
     * @param code the numeric error code
     * @param description the description of the error
     */
    private ErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String toString() {
        return code + ": " + description;
    }
}
