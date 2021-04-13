/*
 */
package keboola.bingads.ex.config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import esnerda.keboola.components.configuration.IKBCParameters;
import esnerda.keboola.components.configuration.ValidationException;
import keboola.bingads.ex.config.pojos.BReportRequest;
import keboola.bingads.ex.config.pojos.BulkRequests;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2015
 */
public class BingParameters  extends IKBCParameters {
    
    private final static String[] REQUIRED_FIELDS = {"devKey", "bucket", "customerId", "bulkRequests", "accountId"};
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
    @JsonProperty("sinceLast")
    private Boolean sinceLast;
    @JsonProperty("bucket")
    private String bucket;
    @JsonProperty("bulkRequests")
    private BulkRequests bulkRequests;
    @JsonProperty("reportRequests")
    private List<BReportRequest> reportRequests;
   
    //debug params
    @JsonProperty("debug")
    private boolean debug;
    @JsonProperty("deb_username")
    private String debUserName;
    @JsonProperty("deb_pass")
    private String debPass;
    @JsonProperty("deb_devkey")
    private String debDevKey;
    
    public BingParameters() {
        parametersMap = new HashMap<>();
        
    }
    
    @JsonCreator
    public BingParameters(@JsonProperty("#devKey") String devKey, @JsonProperty("accountId") Long accountId, @JsonProperty("customerId") Long customerId,
            @JsonProperty("dateTo") String dateTo, @JsonProperty("sinceLast") Boolean sinceLast, @JsonProperty("dateFrom") String dateFrom, @JsonProperty("bucket") String bucket,
            @JsonProperty("bulkRequests") BulkRequests bulkRequests, @JsonProperty("reportRequests") List<BReportRequest> reportRequests,  
            @JsonProperty("debug")  boolean debug, @JsonProperty("deb_username") String debUserName, @JsonProperty("deb_pass") String debPass,
            @JsonProperty ("deb_devkey") String debDevKey
    ) throws ParseException {
        parametersMap = new HashMap<>();
        this.devKey = devKey;
        this.accountId = accountId;
        this.customerId = customerId;
        if (sinceLast == null) {
            this.sinceLast = true;
        } else {
            this.sinceLast = sinceLast;
        }
        
        if (dateFrom != null && !dateFrom.equals("")) {
            setDate_from(dateFrom);
        }
        this.bulkRequests = bulkRequests;
        this.reportRequests = reportRequests;
        this.bucket = bucket;

        this.debug = debug;
        this.debDevKey = debDevKey;
        this.debUserName = debUserName;
        this.debPass = debPass;
        //set param map
        parametersMap.put("devKey", devKey);
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
		boolean res = true;
		for (BReportRequest r : reportRequests) {
			if (!r.validate()) {
				res = false;
			}
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
    
    public Boolean getSinceLast() {
        return sinceLast;
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
    
    public Long getCustomerId() {
        return customerId;
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

    
	public boolean isDebug() {
		return debug;
	}

	public String getDebUserName() {
		return debUserName;
	}

	public String getDebPass() {
		return debPass;
	}

	public String getDebDevKey() {
		return debDevKey;
	}

	@Override
	protected String[] getRequiredFields() {
		return REQUIRED_FIELDS;
	}

	@Override
	protected boolean validateParametres() throws ValidationException {
		// validate date format
		String error = "";

		error += this.missingFieldsMessage(parametersMap);

		if (!getBulkRequests().validate() | !validateReportRequests()) {
			error += "Bulk or Report parameters are specified incorrectly!";

		}

		if (!"".equals(error)) {
			throw new ValidationException("Invalid configuration parameters!", "Config validation error: " + error,
					null);
		}
		return true;
	}

}
