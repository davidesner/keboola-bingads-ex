/*
 */
package keboola.bingads.ex.config.tableconfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * author David Esner <code>&lt;esnerda at gmail.com&gt;</code>
 * created 2016
 */
@JsonInclude(Include.NON_NULL)
public class ManifestFile {

    @JsonProperty("destination")
    private final String destination;
    @JsonProperty("incremental")
    private final boolean incremental;
    //"," default
    @JsonProperty("delimiter")
    private final String delimiter;
    //"\"" default
    @JsonProperty("enclosure")
    private final String enclosure;
    @JsonProperty("primary_key")
    private final String[] primaryKey;
    @JsonProperty("columns")
    private final String[] columns;
    @JsonProperty("rows_count")
    private final Integer rows_count;
    @JsonProperty("data_size_bytes")
    private final Integer data_size_bytes;

    @JsonIgnore
    private String name;

    public ManifestFile(String name, String destination, boolean incremental, String[] primaryKey, String delimiter, String enclosure) {
        this.name = name;
        this.primaryKey = primaryKey;
        this.destination = destination;
        this.incremental = incremental;
        this.delimiter = delimiter;
        this.enclosure = enclosure;

        this.columns = null;
        this.rows_count = null;
        this.data_size_bytes = null;
    }

    @JsonCreator
    public ManifestFile(@JsonProperty("destination") String destination, @JsonProperty("incremental") boolean incremental,
            @JsonProperty("primary_key") String[] primaryKey, @JsonProperty("delimiter") String delimiter,
            @JsonProperty("enclosure") String enclosure, @JsonProperty("rows_count") Integer rows_count,
            @JsonProperty("data_size_bytes") Integer data_size_bytes, @JsonProperty("columns") String[] columns) {

        this.rows_count = rows_count;
        this.data_size_bytes = data_size_bytes;

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
        this.columns = columns;
    }

    private ManifestFile(Builder builder) {
        this.name = builder.name;
        this.rows_count = builder.rows_count;
        this.data_size_bytes = builder.data_size_bytes;
        this.destination = builder.destination;
        this.incremental = builder.incremental;
        this.primaryKey = builder.primaryKey;
        this.delimiter = builder.delimiter;
        this.enclosure = builder.enclosure;
        this.columns = builder.columns;
    }

    public String[] getColumns() {
        return columns;
    }

    public Integer getRows_count() {
        return rows_count;
    }

    public Integer getData_size_bytes() {
        return data_size_bytes;
    }

    public String[] getPrimaryKey() {
        return primaryKey;
    }

    public String getDestination() {
        return destination;
    }

    public boolean isIncremental() {
        return incremental;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public String getEnclosure() {
        return enclosure;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class Builder {

        private final String destination;
        private boolean incremental;

        //"," default
        private String delimiter;

        //"\"" default
        private String enclosure;

        private String[] primaryKey;
        private String[] columns;
        private Integer rows_count;
        private Integer data_size_bytes;
        private final String name;

        /**
         * Creates builder with required parameters set to default values.
         * Default delimiter(,) and enclosure(") characters and incremental load
         * set to false.
         *
         * @param name - name of the source csv table
         * @param destination - destination SAPI path, i.e. in.c-main.tables
         */
        public Builder(String name, String destination) {
            this.name = name;
            this.destination = destination;
            //set default values for required params
            this.delimiter = ",";
            this.enclosure = "\"";
            this.incremental = false;
        }

        public Builder setDelimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public Builder setEnclosure(String enclosure) {
            this.enclosure = enclosure;
            return this;
        }

        public Builder setIncrementalLoad(boolean incremental) {
            this.incremental = incremental;
            return this;
        }

        public Builder setPrimaryKey(String[] pkey) {
            this.primaryKey = pkey;
            return this;
        }

        public Builder setColumns(String[] cols) {
            this.columns = cols;
            return this;
        }

        public Builder setRowsCount(int rCount) {
            this.rows_count = rCount;
            return this;
        }

        public Builder setDataSize(int data_size_bytes) {
            this.data_size_bytes = data_size_bytes;
            return this;
        }

        public ManifestFile build(){
            if (incremental && (primaryKey == null || primaryKey.length == 0)) {
                setIncrementalLoad(false);
            }
            return new ManifestFile(this);
        }

    }
}
