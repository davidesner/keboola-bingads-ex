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

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, int severity) {
        super(message, severity);
    }

}
