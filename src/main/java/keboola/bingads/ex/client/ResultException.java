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

    public ResultException(String message) {
        super(message);
    }

    public ResultException(String message, int severity) {
        super(message, severity);
    }

}
