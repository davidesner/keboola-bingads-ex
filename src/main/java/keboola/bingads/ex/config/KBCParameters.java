/*
 */
package keboola.bingads.ex.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import keboola.bingads.ex.config.pojos.BulkRequests;
import keboola.bingads.ex.config.pojos.BReportRequest;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2015
 */
public class KBCParameters {

    private final static String[] REQUIRED_FIELDS = {"devKey", "bucket", "customerId", "accountId", "bulkRequests"};
    private final Map<String, Object> parametersMap;
    private Date date_from;

    @JsonProperty("#devKey")
    private String devKey;
    @JsonProperty("accountId")
    private Long accountId;
    @JsonProperty("customerId")
    private Long customerId;

    //start date of fetched interval in format: 05-10-2015 21:00
    @JsonProperty("dateFrom")
    private String dateFrom;
    @JsonProperty("bucket")
    private String bucket;
    @JsonProperty("bulkRequests")
    private BulkRequests bulkRequests;
    @JsonProperty("reportRequests")
    private List<BReportRequest> reportRequests;

    public KBCParameters() {
        parametersMap = new HashMap<>();

    }

    @JsonCreator
    public KBCParameters(@JsonProperty("#devKey") String devKey, @JsonProperty("accountId") Long accountId, @JsonProperty("customerId") Long customerId,
            @JsonProperty("dateTo") String dateTo, @JsonProperty("dateFrom") String dateFrom, @JsonProperty("bucket") String bucket,
            @JsonProperty("bulkRequests") BulkRequests bulkRequests, @JsonProperty("reportRequests") List<BReportRequest> reportRequests
    ) throws ParseException {
        parametersMap = new HashMap<>();
        this.devKey = devKey;
        this.accountId = accountId;
        this.customerId = customerId;
        if (dateFrom != null) {
            setDate_from(dateFrom);
        }
        this.bulkRequests = bulkRequests;
        this.reportRequests = reportRequests;
        this.bucket = bucket;

        //set param map
        parametersMap.put("devKey", devKey);
        parametersMap.put("accountId", accountId);
        parametersMap.put("customerId", customerId);
        // parametersMap.put("dateFrom", dateFrom);
        parametersMap.put("bucket", bucket);
        parametersMap.put("bulkRequests", bulkRequests);

    }

    /**
     * Returns list of required fields missing in config
     *
     * @return
     */
    public List<String> getMissingFields() {
        List<String> missing = new ArrayList<String>();
        for (int i = 0; i < REQUIRED_FIELDS.length; i++) {
            Object value = parametersMap.get(REQUIRED_FIELDS[i]);
            if (value == null) {
                missing.add(REQUIRED_FIELDS[i]);
            }
        }

        return missing;
    }

    public boolean validateReportRequests() throws ValidationException {
        boolean res = false;
        for (BReportRequest r : reportRequests) {
            res = r.validate();
        }
        return res;
    }

    private void setDate_from(String dateString) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        this.date_from = format.parse(dateString);

    }

    public Date getDate_from() {
        return date_from;
    }

    public Map<String, Object> getParametersMap() {
        return parametersMap;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public Long getUserId() {
        return customerId;
    }

    public void setUserId(Long customerId) {
        this.customerId = customerId;
    }

    public String getDevKey() {
        return devKey;
    }

    public void setDevKey(String devKey) {
        this.devKey = devKey;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public BulkRequests getBulkRequests() {
        return bulkRequests;
    }

    public List<BReportRequest> getReportRequests() {
        return reportRequests;
    }

}
