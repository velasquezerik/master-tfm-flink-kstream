package com.verisure.xad.poc.inv.kstream.processor.exception;

/**
 * Class for handling service layer exceptions in a common way.
 *
 * @since 1.0.0
 */
public class ProcessException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

    private final ErrorCode code;

    public ProcessException(ErrorCode code) {
		super();
        this.code = code;
	}

    /**
     * Constructs an {@code ProcessException} with the specified detail message and no root cause.
     *
     * @param message
     *            The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     * @param code
     *            The error code identifying the business exception 
     */	
	public ProcessException(String message, ErrorCode code) {
		super(message);
        this.code = code;
	}

    /**
     * Constructs an {@code ProcessException} with the specified root cause.
     *
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated into this
     * exception's detail message.
     *
     * @param cause
     *            The cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value is
     *            permitted, and indicates that the cause is nonexistent or unknown.)
     * @param code
     *            The error code identifying the business exception 
     */
    public ProcessException(Throwable cause, ErrorCode code) {
        super(cause);
        this.code = code;
    }
    /**
     * Constructs an {@code ProcessException} with the specified detail message and root cause.
     *
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i> automatically incorporated into this
     * exception's detail message.
     *
     * @param message
     *            The detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     *
     * @param cause
     *            The cause (which is saved for later retrieval by the {@link #getCause()} method). (A null value is
     *            permitted, and indicates that the cause is nonexistent or unknown.)
     * @param code
     *            The error code identifying the business exception 
     */    
	public ProcessException(String message, Throwable cause, ErrorCode code) {
		super(message, cause);
        this.code = code;
	}

    /**
     * Returns the error code.
     * @return The error code with a given identifier and a short description.
     */
    public ErrorCode getCode() {
		return this.code;
	}

}
