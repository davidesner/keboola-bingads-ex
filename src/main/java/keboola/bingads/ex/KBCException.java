/*
 */
package keboola.bingads.ex;

/**
 * Abstract class implementing exception with severity indicator
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public abstract class KBCException extends Exception {

    private final int severity;

    /**
     * Create exception with default severity = 1 (User Exception)
     *
     * @param message
     */
    public KBCException(String message) {
        super(message);
        severity = 1;
    }

    /**
     * Create exception with custom severity
     *
     * @param message
     * @param severity - severity code (Exit status = 1 will be considered as an
     * user exception, all other as application exceptions.)
     */
    public KBCException(String message, int severity) {
        super(message);
        this.severity = severity;
    }

    public int getSeverity() {
        return severity;
    }

}
