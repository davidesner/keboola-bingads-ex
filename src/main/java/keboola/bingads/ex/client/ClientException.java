/*
 */
package keboola.bingads.ex.client;

import keboola.bingads.ex.KBCException;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class ClientException extends KBCException {

    public ClientException(String message, Exception cause) {
        super(message, message,  cause);
    }

    public ClientException(String message, int severity, Exception cause) {
        super(message, severity, cause);
    }

}
