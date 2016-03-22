/*
 */
package keboola.bingads.ex.config.tableconfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class ManifestFile {

    @JsonProperty("destination")
    private String destination;
    @JsonProperty("incremental")
    private boolean incremental;
    //"," default
    @JsonProperty("delimiter")
    private String delimiter;
    //"\"" default
    @JsonProperty("enclosure")
    private String enclosure;
    @JsonProperty("primary_key")
    private String[] primaryKey;

    @JsonCreator
    public ManifestFile(@JsonProperty("destination") String destination, @JsonProperty("incremental") boolean incremental,
            @JsonProperty("primary_key") String[] primaryKey, @JsonProperty("delimiter") String delimiter, @JsonProperty("enclosure") String enclosure) {

        this.destination = destination;
        this.incremental = incremental;
        this.primaryKey = primaryKey;
        //default values
        if (delimiter == null) {
            this.delimiter = ",";
        } else {
            this.delimiter = delimiter;
        }
        if (enclosure == null) {
            this.enclosure = "\"";
        } else {
            this.enclosure = enclosure;
        }
    }

    public String[] getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String[] primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public boolean isIncremental() {
        return incremental;
    }

    public void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getEnclosure() {
        return enclosure;
    }

    public void setEnclosure(String enclosure) {
        this.enclosure = enclosure;
    }

}
