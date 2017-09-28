package keboola.bingads.ex;

/*
 */

/**
 * Abstract class implementing exception with severity indicator
 *
 * author David Esner <code>&lt;esnerda at gmail.com&gt;</code>
 * created 2016
 */
public class KBCException extends Exception {

    /**
     * Defines severity level of the exception.
     * 0 - warning
     * 1 - user exception
     * 2 - error
     *
     */
    private final int severity;

    private final String detailedMessage;

    private final Object details;

    /**
     * Create exception with default severity = 1 (User Exception)
     *
     * @param message
     * @param detailedMessage
     * @param details
     */
    public KBCException(String message, String detailedMessage, Object details) {
        super(message);
        this.detailedMessage = detailedMessage;
        this.details = details;
        severity = 1;
    }

    /**
     * Create exception with custom severity
     *
     * @param message
     * @param detailedMessage
     * @param details
     * @param severity - severity code (Exit status = 1 will be considered as an
     * user exception, all other as application exceptions.)
     */
    public KBCException(String message, String detailedMessage, Object details, int severity) {
        super(message);
        this.severity = severity;
        this.detailedMessage = detailedMessage;
        this.details = details;
    }

    public KBCException(String detailedMessage, int severity, Exception cause){
    	super(detailedMessage, cause);
    	this.severity = severity;
    	this.details = null;
    	this.detailedMessage = detailedMessage;
    }
    

    public int getSeverity() {
        return severity;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public Object getDetails() {
        return details;
    }

}
