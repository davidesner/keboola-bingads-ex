/*
 */
package keboola.bingads.ex.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2015
 */
public class KBCTablesList {

    @JsonProperty("tables")
    private Map<Integer, KBCOutputMapping> tables;

    public KBCTablesList() {
    }

    public KBCTablesList(Map<Integer, KBCOutputMapping> tables) {
        this.tables = tables;
    }

    public Map<Integer, KBCOutputMapping> getTables() {
        return tables;
    }

    public void setTables(Map<Integer, KBCOutputMapping> tables) {
        this.tables = tables;
    }
}
