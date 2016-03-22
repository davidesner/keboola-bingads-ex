/*
 */
package keboola.bingads.ex.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2015
 */
public class KBCOutputMapping {

    @JsonProperty("source")
    private String source;
    @JsonProperty("destination")
    private String destination;

    public KBCOutputMapping() {
    }

    public KBCOutputMapping(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
