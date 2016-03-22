/*
 */
package keboola.bingads.ex.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class LastState {

    @JsonProperty("bulkRequests")
    private Map<String, Date> bulkRequests;

    @JsonProperty("reportRequests")
    private Map<String, Date> reportRequests;

    public LastState(Map<String, Date> bulkRequests, Map<String, Date> reportRequests) {
        this.bulkRequests = bulkRequests;
        this.reportRequests = reportRequests;
    }

    public Map<String, Date> getBulkRequests() {
        return bulkRequests;
    }

    public void setBulkRequests(Map<String, Date> bulkRequests) {
        this.bulkRequests = bulkRequests;
    }

    public Map<String, Date> getReportRequests() {
        return reportRequests;
    }

    public void setReportRequests(Map<String, Date> reportRequests) {
        this.reportRequests = reportRequests;
    }

    public LastState() {
    }

}
