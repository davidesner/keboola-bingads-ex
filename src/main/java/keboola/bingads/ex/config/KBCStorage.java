/*
 */
package keboola.bingads.ex.config;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2015
 */
public class KBCStorage {

    @JsonProperty("output")
    private KBCTablesList outputTables;

    public KBCStorage() {
    }

    public KBCStorage(KBCTablesList outputTables) {
        this.outputTables = outputTables;
    }

    public KBCTablesList getOutputTables() {
        return outputTables;
    }

    public void setOutputTables(KBCTablesList outputTables) {
        this.outputTables = outputTables;
    }
}
