/*
 */
package keboola.bingads.ex.client;

import keboola.bingads.ex.KBCException;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class ResultException extends KBCException {

    public ResultException(String message, Exception cause) {
        super(message, message,  cause);
    }

    public ResultException(String message, int severity, Exception cause) {
        super(message, severity, cause);
    }

}
